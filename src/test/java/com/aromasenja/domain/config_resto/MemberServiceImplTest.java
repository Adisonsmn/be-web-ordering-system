package com.aromasenja.domain.config_resto;

import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.domain.config_resto.dto.MemberResponse;
import com.aromasenja.domain.config_resto.dto.UpdateMemberRequest;
import com.aromasenja.domain.pesanan.PesananRepository;
import com.aromasenja.domain.poin.PoinMapper;
import com.aromasenja.domain.poin.PoinTransaksiRepository;
import com.aromasenja.domain.poin.dto.PoinRiwayatResponse;
import com.aromasenja.domain.poin.entity.PoinTransaksi;
import com.aromasenja.domain.user.ClientRepository;
import com.aromasenja.domain.user.UserRepository;
import com.aromasenja.domain.user.entity.Client;
import com.aromasenja.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberServiceImpl Unit Tests")
class MemberServiceImplTest {

    @Mock private ClientRepository clientRepository;
    @Mock private UserRepository userRepository;
    @Mock private PesananRepository pesananRepository;
    @Mock private PoinTransaksiRepository poinTransaksiRepository;
    @Mock private PoinMapper poinMapper;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Client client;
    private User user;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("John Doe");
        user.setEmail("john@doe.com");
        user.setPhone("0812");
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);

        client = new Client();
        client.setClientId(clientId);
        client.setUser(user);
        client.setTotalPoint(100);
    }

    @Test
    @DisplayName("Get All Members - Success")
    void getAllMembers_Success() {
        Page<Client> clientPage = new PageImpl<>(Collections.singletonList(client));
        when(clientRepository.searchMembers(eq("John"), any(Pageable.class))).thenReturn(clientPage);
        when(pesananRepository.findLastOrderTimeByClientId(clientId)).thenReturn(null);

        Page<MemberResponse> response = memberService.getAllMembers("John", Pageable.unpaged());

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).name()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Get Member Poin Riwayat - Success")
    void getMemberPoinRiwayat_Success() {
        when(clientRepository.existsById(clientId)).thenReturn(true);
        PoinTransaksi trans = new PoinTransaksi();
        Page<PoinTransaksi> page = new PageImpl<>(Collections.singletonList(trans));
        when(poinTransaksiRepository.findByClientClientIdOrderByCreatedAtDesc(eq(clientId), any(Pageable.class)))
                .thenReturn(page);
        
        PoinRiwayatResponse riwayatResponse = new PoinRiwayatResponse(
                UUID.randomUUID(), UUID.randomUUID(), "ORD-1", 10, "earn", LocalDateTime.now()
        );
        when(poinMapper.toResponse(trans)).thenReturn(riwayatResponse);

        Page<PoinRiwayatResponse> response = memberService.getMemberPoinRiwayat(clientId, Pageable.unpaged());

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).kodePesanan()).isEqualTo("ORD-1");
    }

    @Test
    @DisplayName("Get Member Poin Riwayat - Client Not Found")
    void getMemberPoinRiwayat_NotFound() {
        when(clientRepository.existsById(clientId)).thenReturn(false);

        assertThatThrownBy(() -> memberService.getMemberPoinRiwayat(clientId, Pageable.unpaged()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Update Member - Success")
    void updateMember_Success() {
        UpdateMemberRequest request = new UpdateMemberRequest("Jane Doe", "0899", false);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        MemberResponse response = memberService.updateMember(clientId, request);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Jane Doe");
        assertThat(response.isActive()).isFalse();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Soft Delete Member - Success")
    void softDeleteMember_Success() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        memberService.softDeleteMember(clientId);

        assertThat(user.isActive()).isFalse();
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Export Members - Success")
    void exportMembers_Success() {
        when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));
        when(pesananRepository.findLastOrderTimeByClientId(clientId)).thenReturn(null);

        ByteArrayInputStream in = memberService.exportMembersToExcel();

        assertThat(in).isNotNull();
        assertThat(in.available()).isGreaterThan(0);
    }
}
