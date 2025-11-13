package com.QuanTech.QuanTech.services;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.QuanTech.QuanTech.constants.ErrorConstants;
import com.QuanTech.QuanTech.constants.UuidErrorConstants;
import com.QuanTech.QuanTech.constants.enums.ShiftStatus;
import com.QuanTech.QuanTech.constants.enums.ShiftSwapRequestStatus;
import com.QuanTech.QuanTech.dto.shiftSwapRequest.CreateShiftSwapRequestDTO;
import com.QuanTech.QuanTech.dto.shiftSwapRequest.ShiftSwapQueryResponseDTO;
import com.QuanTech.QuanTech.dto.shiftSwapRequest.ShiftSwapResponseDTO;
import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.entity.Shift;
import com.QuanTech.QuanTech.entity.ShiftSwapRequest;
import com.QuanTech.QuanTech.exception.custom.ResourceNotFoundException;
import com.QuanTech.QuanTech.exception.custom.ShiftSwapRequestException;
import com.QuanTech.QuanTech.repository.EmployeeRepository;
import com.QuanTech.QuanTech.repository.ShiftRepository;
import com.QuanTech.QuanTech.repository.ShiftSwapRepository;
import com.QuanTech.QuanTech.services.interfaces.ShiftSwapRequestService;
import com.QuanTech.QuanTech.util.NanoIdGenerator;
import com.QuanTech.QuanTech.util.ShiftSwapValidators;
import com.QuanTech.QuanTech.util.mappers.ShiftSwapMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static com.QuanTech.QuanTech.util.ParseUUID.parseUUID;

@Service
public class ShiftSwapRequestServiceImpl implements ShiftSwapRequestService {
    private final ShiftSwapRepository shiftSwapRepository;
    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ShiftSwapRequestServiceImpl(
            ShiftSwapRepository shiftSwapRepository,
            ShiftRepository shiftRepository,
            EmployeeRepository employeeRepository
    ) {
        this.shiftSwapRepository = shiftSwapRepository;
        this.shiftRepository = shiftRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public ShiftSwapResponseDTO createSwapRequest(CreateShiftSwapRequestDTO createSwapDto) {
        UUID requesterId = createSwapDto.requesterEmployeeId();
        UUID requestedId = createSwapDto.requestedEmployeeId();


        if (requesterId.equals(requestedId)) {
            throw new ResourceNotFoundException(ErrorConstants.DIFF_REQUESTER_REQUESTED_EMP);
        }

        if (createSwapDto.requestedEmployeeId() == null || createSwapDto.offeringShiftId() == null || createSwapDto.requestingShiftId() == null) {
            throw new ShiftSwapRequestException(ErrorConstants.ALL_IDS_REQUIRED);
        }

        Employee requester = employeeRepository.findById(createSwapDto.requesterEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.REQUESTER_NOT_FOUND));

        Employee requested = employeeRepository.findById(createSwapDto.requestedEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.REQUESTED_NOT_FOUND));

        if (requester.getTeam() == null || requested.getTeam() == null || !requester.getTeam().getId().equals(requested.getTeam().getId())) {
            throw new ShiftSwapRequestException(ErrorConstants.EMPLOYEE_NOT_IN_MANAGER_TEAM);
        }

        if(requester.getTeam().getTeamManager() != null && requested.getId().equals(requester.getTeam().getTeamManager().getId())) {
            throw new ShiftSwapRequestException("Cannot request swap with team manager");
        }

        Shift offeringShift = shiftRepository.findById(createSwapDto.offeringShiftId())
                .orElseThrow(() -> new ShiftSwapRequestException(ErrorConstants.OFFERING_SHIFT_NOT_FOUND));

        Shift requestingShift = shiftRepository.findById(createSwapDto.requestingShiftId())
                .orElseThrow(() -> new ShiftSwapRequestException(ErrorConstants.REQUESTING_SHIFT_NOT_FOUND));


        if (!offeringShift.getEmployee().getId().equals(requester.getId())) {
            throw new ShiftSwapRequestException(ErrorConstants.INVALID_OFFERING_SHIFT);
        }

        if (!requestingShift.getEmployee().getId().equals(requested.getId())) {
            throw new ShiftSwapRequestException(ErrorConstants.INVALID_REQUESTING_SHIFT);
        }


        var now = OffsetDateTime.now();
        if(offeringShift.getShiftStartTime().isBefore(now) || requestingShift.getShiftStartTime().isBefore(now)) {
            throw new IllegalArgumentException("Cannot swap shifts that have already started");
        }

        ShiftSwapRequest shiftSwapEntity = new ShiftSwapRequest();

        int shiftSwapIdLength = 10;
        String nanoId = NanoIdUtils.randomNanoId(
                NanoIdGenerator.DEFAULT_NUMBER_GENERATOR,
                NanoIdGenerator.DEFAULT_ALPHABET,
                shiftSwapIdLength
        );

        offeringShift.setShiftStatus(ShiftStatus.PENDING);
        requestingShift.setShiftStatus(ShiftStatus.PENDING);
        shiftRepository.save(offeringShift);
        shiftRepository.save(requestingShift);

        shiftSwapEntity.setPublicId("SSR-" + nanoId);
        shiftSwapEntity.setRequester(requester);
        shiftSwapEntity.setRequested(requested);
        shiftSwapEntity.setOfferingShift(offeringShift);
        shiftSwapEntity.setRequestingShift(requestingShift);
        shiftSwapEntity.setStatus(ShiftSwapRequestStatus.PENDING);
        shiftSwapEntity.setReason(createSwapDto.reason());
        shiftSwapEntity.setApprovedBy(null);
        shiftSwapEntity.setApprovedDate(null);

        ShiftSwapRequest savedSwap = shiftSwapRepository.save(shiftSwapEntity);

        return ShiftSwapMapper.shiftSwapEntityToDto(savedSwap);
    }


    @Override
    public List<ShiftSwapQueryResponseDTO> getSwapRequestsForEmployee(String employeeId) {
        UUID requesterOrRequestedID = parseUUID(employeeId, UuidErrorConstants.INVALID_REQUESTER_OR_REQUESTED_ID);

        return shiftSwapRepository.findSwapResponsesByEmployeeId(requesterOrRequestedID);
    }

    @Override
    public List<ShiftSwapQueryResponseDTO> getTeamSwapRequests(String managerId) {
        UUID mngID = parseUUID(managerId, UuidErrorConstants.INVALID_MANAGER_UUID);

//        List<ShiftSwapQueryResponseDTO> requests = shiftSwapRepository.findShiftSwapRequestsOfTeamByManagerId(mngID);
//        return requests.stream().map(ShiftSwapMapper::shiftSwapEntityToDto).toList();
        return shiftSwapRepository.findShiftSwapRequestsOfTeamByManagerId(mngID);
    }

    @Override
    @Transactional
    public ShiftSwapResponseDTO approveSwapRequest(String managerId, String swapRequestId) {
        UUID mngID = parseUUID(managerId, UuidErrorConstants.INVALID_MANAGER_UUID);
        UUID swapReqID = parseUUID(swapRequestId, UuidErrorConstants.INVALID_SWAP_REQUEST_ID);

        Employee manager = employeeRepository.findById(mngID)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.MANAGER_NOT_FOUND));

        ShiftSwapRequest shiftSwapRequest = shiftSwapRepository.findById(swapReqID)
                .orElseThrow(() -> new ShiftSwapRequestException(ErrorConstants.SWAP_REQUEST_NOT_FOUND));

        // checking if the curr swap req is in pending stage or not
        ShiftSwapValidators.ensurePending(shiftSwapRequest);
        ShiftSwapValidators.ensureManagerOwnsTeam(manager, shiftSwapRequest);

        // get the offering and requesting shift
        Shift offeringShift = shiftSwapRequest.getOfferingShift();
        Shift requestingShift = shiftSwapRequest.getRequestingShift();

        // then just get the employees from the request
        Employee requester = shiftSwapRequest.getRequester();
        Employee requested = shiftSwapRequest.getRequested();

        // after that just swap the requests
        offeringShift.setEmployee(requested);
        requestingShift.setEmployee(requester);
        offeringShift.setShiftStatus(ShiftStatus.CONFIRMED);
        requestingShift.setShiftStatus(ShiftStatus.CONFIRMED);

        // finally save that using the repo
        shiftRepository.save(offeringShift);
        shiftRepository.save(requestingShift);


        // .. then just mark that swap request as approved by setting the required values
        shiftSwapRequest.setStatus(ShiftSwapRequestStatus.APPROVED);
        shiftSwapRequest.setApprovedBy(manager);
        shiftSwapRequest.setApprovedDate(OffsetDateTime.now());

        ShiftSwapRequest savedSwap = shiftSwapRepository.save(shiftSwapRequest);

        return ShiftSwapMapper.shiftSwapEntityToDto(savedSwap);
    }

    @Override
    @Transactional
    public ShiftSwapResponseDTO rejectSwapRequest(String managerId, String swapRequestId) {
        UUID mngID = parseUUID(managerId, UuidErrorConstants.INVALID_MANAGER_UUID);
        UUID swapReqID = parseUUID(swapRequestId, UuidErrorConstants.INVALID_SWAP_REQUEST_ID);

        Employee manager = employeeRepository.findById(mngID)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.MANAGER_NOT_FOUND));

        ShiftSwapRequest shiftSwapRequest = shiftSwapRepository.findById(swapReqID)
                .orElseThrow(() -> new ShiftSwapRequestException(ErrorConstants.SWAP_REQUEST_NOT_FOUND));

        ShiftSwapValidators.ensurePending(shiftSwapRequest);
        ShiftSwapValidators.ensureManagerOwnsTeam(manager, shiftSwapRequest);

        // just only mark the swap request as rejected
        // don't have to do anything in the shift repo
        Shift offeringShift = shiftSwapRequest.getOfferingShift();
        Shift requestingShift = shiftSwapRequest.getRequestingShift();


        // reason: if rejected i want to keep the original shifts as it is
        // otherwise the employee might think it as his/her shift is cancelled and that is a holiday
        // for that keeping the status to Confirmed will make sense rather than rejecting it
        offeringShift.setShiftStatus(ShiftStatus.CONFIRMED);
        requestingShift.setShiftStatus(ShiftStatus.CONFIRMED);
        shiftRepository.save(offeringShift);
        shiftRepository.save(requestingShift);


        shiftSwapRequest.setStatus(ShiftSwapRequestStatus.REJECTED);
        shiftSwapRequest.setApprovedBy(manager);
        shiftSwapRequest.setApprovedDate(OffsetDateTime.now());

        ShiftSwapRequest savedSwap = shiftSwapRepository.save(shiftSwapRequest);

        return ShiftSwapMapper.shiftSwapEntityToDto(savedSwap);
    }
}
