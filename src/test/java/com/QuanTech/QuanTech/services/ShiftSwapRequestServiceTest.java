package com.QuanTech.QuanTech.services;

import com.QuanTech.QuanTech.constants.enums.ShiftSwapRequestStatus;
import com.QuanTech.QuanTech.dto.shiftSwapRequest.CreateShiftSwapRequestDTO;
import com.QuanTech.QuanTech.dto.shiftSwapRequest.ShiftSwapQueryResponseDTO;
import com.QuanTech.QuanTech.dto.shiftSwapRequest.ShiftSwapResponseDTO;
import com.QuanTech.QuanTech.entity.Employee;
import com.QuanTech.QuanTech.entity.Shift;
import com.QuanTech.QuanTech.entity.ShiftSwapRequest;
import com.QuanTech.QuanTech.entity.Team;
import com.QuanTech.QuanTech.repository.EmployeeRepository;
import com.QuanTech.QuanTech.repository.ShiftRepository;
import com.QuanTech.QuanTech.repository.ShiftSwapRepository;
import com.QuanTech.QuanTech.services.ShiftSwapRequestServiceImpl;
import com.QuanTech.QuanTech.util.mappers.ShiftSwapMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShiftSwapRequestServiceTest {

    @Mock
    private ShiftSwapRepository shiftSwapRepository;

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ShiftSwapRequestServiceImpl shiftSwapService;

    private UUID requesterId;
    private UUID requestedId;
    private UUID offeringShiftId;
    private UUID requestingShiftId;
    private OffsetDateTime now;

    @BeforeEach
    public void setUp() {
        requesterId = UUID.randomUUID();
        requestedId = UUID.randomUUID();
        offeringShiftId = UUID.randomUUID();
        requestingShiftId = UUID.randomUUID();
        now = OffsetDateTime.now().plusHours(1); // ensure future times for creation tests
    }

    @Test
    public void createSwapRequest_success() {
        CreateShiftSwapRequestDTO createDto = new CreateShiftSwapRequestDTO(
                requesterId,
                requestedId,
                offeringShiftId,
                requestingShiftId,
                "Please swap"
        );

        Employee requester = new Employee();
        requester.setId(requesterId);
        Team team = new Team();
        team.setId(UUID.randomUUID());
        requester.setTeam(team);

        Employee requested = new Employee();
        requested.setId(requestedId);
        requested.setTeam(team);

        Shift offeringShift = new Shift();
        offeringShift.setId(offeringShiftId);
        offeringShift.setEmployee(requester);
        offeringShift.setShiftStartTime(now.plusHours(2));

        Shift requestingShift = new Shift();
        requestingShift.setId(requestingShiftId);
        requestingShift.setEmployee(requested);
        requestingShift.setShiftStartTime(now.plusHours(3));

        when(employeeRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(employeeRepository.findById(requestedId)).thenReturn(Optional.of(requested));
        when(shiftRepository.findById(offeringShiftId)).thenReturn(Optional.of(offeringShift));
        when(shiftRepository.findById(requestingShiftId)).thenReturn(Optional.of(requestingShift));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shiftSwapRepository.save(any(ShiftSwapRequest.class))).thenAnswer(invocation -> {
            ShiftSwapRequest arg = invocation.getArgument(0);
            arg.setId(UUID.randomUUID());
            return arg;
        });

        ShiftSwapResponseDTO.ShiftInfo offeringInfo = new ShiftSwapResponseDTO.ShiftInfo(
                offeringShiftId, "DAY", offeringShift.getShiftStartTime().toLocalDate().atStartOfDay().atOffset(offeringShift.getShiftStartTime().getOffset()), offeringShift.getShiftStartTime(), offeringShift.getShiftStartTime().plusHours(8), "LocA"
        );
        ShiftSwapResponseDTO.ShiftInfo requestingInfo = new ShiftSwapResponseDTO.ShiftInfo(
                requestingShiftId, "NIGHT", requestingShift.getShiftStartTime().toLocalDate().atStartOfDay().atOffset(requestingShift.getShiftStartTime().getOffset()), requestingShift.getShiftStartTime(), requestingShift.getShiftStartTime().plusHours(8), "LocB"
        );
        ShiftSwapResponseDTO expectedDto = new ShiftSwapResponseDTO(
                UUID.randomUUID(),
                "SSR-123",
                "From",
                "To",
                null,
                offeringInfo,
                requestingInfo,
                "Please swap",
                null,
                null
        );

        try (MockedStatic<ShiftSwapMapper> mapper = Mockito.mockStatic(ShiftSwapMapper.class)) {
            mapper.when(() -> ShiftSwapMapper.shiftSwapEntityToDto(any(ShiftSwapRequest.class))).thenReturn(expectedDto);

            ShiftSwapResponseDTO result = shiftSwapService.createSwapRequest(createDto);

            assertNotNull(result);
            assertEquals("SSR-123", result.shiftSwapId());
            verify(shiftRepository, times(2)).save(any(Shift.class));
            verify(shiftSwapRepository, times(1)).save(any(ShiftSwapRequest.class));
        }
    }

    @Test
    public void createSwapRequest_sameEmployee_throws() {
        CreateShiftSwapRequestDTO createDto = new CreateShiftSwapRequestDTO(
                requesterId,
                requesterId,
                offeringShiftId,
                requestingShiftId,
                "Reason"
        );

        assertThrows(RuntimeException.class, () -> shiftSwapService.createSwapRequest(createDto));
        verifyNoInteractions(employeeRepository);
    }

    @Test
    public void getSwapRequestsForEmployee_returnsList() {
        UUID employeeId = UUID.randomUUID();

        ShiftSwapQueryResponseDTO dto = new ShiftSwapQueryResponseDTO(
                UUID.randomUUID(),
                "SSR-001",
                "Alice",
                "Bob",
                ShiftSwapRequestStatus.PENDING,
                "DAY",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "LocA",
                "NIGHT",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "LocB",
                "Reason",
                null,
                null
        );

        when(shiftSwapRepository.findSwapResponsesByEmployeeId(employeeId)).thenReturn(List.of(dto));

        var res = shiftSwapService.getSwapRequestsForEmployee(employeeId.toString());

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals("SSR-001", res.get(0).shiftSwapId());
    }

    @Test
    public void getTeamSwapRequests_returnsList() {
        UUID managerId = UUID.randomUUID();

        ShiftSwapQueryResponseDTO dto = new ShiftSwapQueryResponseDTO(
                UUID.randomUUID(),
                "SSR-002",
                "Carol",
                "Dave",
                ShiftSwapRequestStatus.PENDING,
                "DAY",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "LocX",
                "NIGHT",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "LocY",
                "Reason 2",
                null,
                null
        );

        when(shiftSwapRepository.findShiftSwapRequestsOfTeamByManagerId(managerId)).thenReturn(List.of(dto));

        var res = shiftSwapService.getTeamSwapRequests(managerId.toString());

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals("SSR-002", res.get(0).shiftSwapId());
    }

    @Test
    public void approveSwapRequest_success() {
        UUID managerId = UUID.randomUUID();
        UUID swapReqId = UUID.randomUUID();

        Employee manager = new Employee();
        manager.setId(managerId);

        Employee requester = new Employee();
        requester.setId(UUID.randomUUID());
        Employee requested = new Employee();
        requested.setId(UUID.randomUUID());

        Team requesterTeam = new Team();
        requesterTeam.setTeamManager(manager);
        requester.setTeam(requesterTeam);

        Team requestedTeam = new Team();
        requestedTeam.setTeamManager(manager);
        requested.setTeam(requestedTeam);

        Shift offeringShift = new Shift();
        offeringShift.setId(UUID.randomUUID());
        offeringShift.setEmployee(requester);
        offeringShift.setShiftStartTime(now.plusHours(5));

        Shift requestingShift = new Shift();
        requestingShift.setId(UUID.randomUUID());
        requestingShift.setEmployee(requested);
        requestingShift.setShiftStartTime(now.plusHours(6));

        ShiftSwapRequest swap = new ShiftSwapRequest();
        swap.setId(swapReqId);
        swap.setPublicId("SSR-APPR");
        swap.setRequester(requester);
        swap.setRequested(requested);
        swap.setOfferingShift(offeringShift);
        swap.setRequestingShift(requestingShift);
        swap.setStatus(ShiftSwapRequestStatus.PENDING);

        when(employeeRepository.findById(managerId)).thenReturn(Optional.of(manager));
        when(shiftSwapRepository.findById(swapReqId)).thenReturn(Optional.of(swap));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shiftSwapRepository.save(any(ShiftSwapRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShiftSwapResponseDTO.ShiftInfo offeringInfo = new ShiftSwapResponseDTO.ShiftInfo(
                offeringShift.getId(), "DAY", offeringShift.getShiftStartTime(), offeringShift.getShiftStartTime(), offeringShift.getShiftStartTime().plusHours(8), "LocA"
        );
        ShiftSwapResponseDTO.ShiftInfo requestingInfo = new ShiftSwapResponseDTO.ShiftInfo(
                requestingShift.getId(), "NIGHT", requestingShift.getShiftStartTime(), requestingShift.getShiftStartTime(), requestingShift.getShiftStartTime().plusHours(8), "LocB"
        );
        ShiftSwapResponseDTO expectedDto = new ShiftSwapResponseDTO(
                UUID.randomUUID(),
                "SSR-APPR",
                "Eve",
                "Frank",
                ShiftSwapRequestStatus.APPROVED,
                offeringInfo,
                requestingInfo,
                "Approved reason",
                "Manager One",
                OffsetDateTime.now()
        );

        try (MockedStatic<ShiftSwapMapper> mapper = Mockito.mockStatic(ShiftSwapMapper.class)) {
            mapper.when(() -> ShiftSwapMapper.shiftSwapEntityToDto(any(ShiftSwapRequest.class))).thenReturn(expectedDto);

            ShiftSwapResponseDTO result = shiftSwapService.approveSwapRequest(managerId.toString(), swapReqId.toString());

            assertNotNull(result);
            assertEquals("SSR-APPR", result.shiftSwapId());
            assertEquals("Manager One", result.approvedByName());

            verify(shiftRepository, times(2)).save(any(Shift.class));
            verify(shiftSwapRepository, times(1)).save(any(ShiftSwapRequest.class));
        }
    }

    @Test
    public void rejectSwapRequest_success() {
        UUID managerId = UUID.randomUUID();
        UUID swapReqId = UUID.randomUUID();

        Employee manager = new Employee();
        manager.setId(managerId);

        Employee requester = new Employee();
        requester.setId(UUID.randomUUID());
        Employee requested = new Employee();
        requested.setId(UUID.randomUUID());

        Team requesterTeam = new Team();
        requesterTeam.setTeamManager(manager);
        requester.setTeam(requesterTeam);

        Team requestedTeam = new Team();
        requestedTeam.setTeamManager(manager);
        requested.setTeam(requestedTeam);

        Shift offeringShift = new Shift();
        offeringShift.setId(UUID.randomUUID());
        offeringShift.setEmployee(requester);
        offeringShift.setShiftStartTime(now.plusHours(5));

        Shift requestingShift = new Shift();
        requestingShift.setId(UUID.randomUUID());
        requestingShift.setEmployee(requested);
        requestingShift.setShiftStartTime(now.plusHours(6));

        ShiftSwapRequest swap = new ShiftSwapRequest();
        swap.setId(swapReqId);
        swap.setPublicId("SSR-REJ");
        swap.setRequester(requester);
        swap.setRequested(requested);
        swap.setOfferingShift(offeringShift);
        swap.setRequestingShift(requestingShift);
        swap.setStatus(ShiftSwapRequestStatus.PENDING);

        when(employeeRepository.findById(managerId)).thenReturn(Optional.of(manager));
        when(shiftSwapRepository.findById(swapReqId)).thenReturn(Optional.of(swap));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shiftSwapRepository.save(any(ShiftSwapRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShiftSwapResponseDTO.ShiftInfo offeringInfo = new ShiftSwapResponseDTO.ShiftInfo(
                offeringShift.getId(), "DAY", offeringShift.getShiftStartTime(), offeringShift.getShiftStartTime(), offeringShift.getShiftStartTime().plusHours(8), "LocA"
        );
        ShiftSwapResponseDTO.ShiftInfo requestingInfo = new ShiftSwapResponseDTO.ShiftInfo(
                requestingShift.getId(), "NIGHT", requestingShift.getShiftStartTime(), requestingShift.getShiftStartTime(), requestingShift.getShiftStartTime().plusHours(8), "LocB"
        );
        ShiftSwapResponseDTO expectedDto = new ShiftSwapResponseDTO(
                UUID.randomUUID(),
                "SSR-REJ",
                "Gina",
                "Hank",
                ShiftSwapRequestStatus.REJECTED,
                offeringInfo,
                requestingInfo,
                "Rejected reason",
                "Manager Two",
                OffsetDateTime.now()
        );

        try (MockedStatic<ShiftSwapMapper> mapper = Mockito.mockStatic(ShiftSwapMapper.class)) {
            mapper.when(() -> ShiftSwapMapper.shiftSwapEntityToDto(any(ShiftSwapRequest.class))).thenReturn(expectedDto);

            ShiftSwapResponseDTO result = shiftSwapService.rejectSwapRequest(managerId.toString(), swapReqId.toString());

            assertNotNull(result);
            assertEquals("SSR-REJ", result.shiftSwapId());
            assertEquals("Manager Two", result.approvedByName());

            verify(shiftRepository, times(2)).save(any(Shift.class));
            verify(shiftSwapRepository, times(1)).save(any(ShiftSwapRequest.class));
        }
    }
}