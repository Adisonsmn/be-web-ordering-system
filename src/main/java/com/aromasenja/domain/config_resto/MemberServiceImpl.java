package com.aromasenja.domain.config_resto;

import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.domain.config_resto.dto.MemberResponse;
import com.aromasenja.domain.config_resto.dto.UpdateMemberRequest;
import com.aromasenja.domain.pesanan.PesananRepository;
import com.aromasenja.domain.poin.PoinMapper;
import com.aromasenja.domain.poin.PoinTransaksiRepository;
import com.aromasenja.domain.poin.dto.PoinRiwayatResponse;
import com.aromasenja.domain.user.ClientRepository;
import com.aromasenja.domain.user.UserRepository;
import com.aromasenja.domain.user.entity.Client;
import com.aromasenja.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MemberServiceImpl.class);

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PesananRepository pesananRepository;
    private final PoinTransaksiRepository poinTransaksiRepository;
    private final PoinMapper poinMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<MemberResponse> getAllMembers(String search, Pageable pageable) {
        Page<Client> clientPage = clientRepository.searchMembers(search, pageable);
        List<MemberResponse> responses = clientPage.getContent().stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, clientPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PoinRiwayatResponse> getMemberPoinRiwayat(UUID clientId, Pageable pageable) {
        // Pastikan client ada
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client dengan ID " + clientId + " tidak ditemukan");
        }
        return poinTransaksiRepository.findByClientClientIdOrderByCreatedAtDesc(clientId, pageable)
                .map(poinMapper::toResponse);
    }

    @Override
    @Transactional
    public MemberResponse updateMember(UUID clientId, UpdateMemberRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client dengan ID " + clientId + " tidak ditemukan"));

        User user = client.getUser();
        user.setName(request.name());
        user.setPhone(request.phone());
        user.setActive(request.isActive());

        userRepository.save(user);
        log.info("Member profile updated successfully: clientId={}, name={}", clientId, request.name());

        return mapToMemberResponse(client);
    }

    @Override
    @Transactional
    public void softDeleteMember(UUID clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client dengan ID " + clientId + " tidak ditemukan"));

        User user = client.getUser();
        user.setActive(false);
        userRepository.save(user);
        log.info("Member soft deleted successfully: clientId={}", clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportMembersToExcel() {
        List<Client> clients = clientRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Daftar Member");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Create Headers
            Row headerRow = sheet.createRow(0);
            String[] columns = {"No", "Nama", "Email", "Telepon", "Total Poin", "Status Member", "Tanggal Daftar", "Last Order", "Status Aktif"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate rows
            int rowIdx = 1;
            for (Client client : clients) {
                Row row = sheet.createRow(rowIdx++);
                User user = client.getUser();

                LocalDateTime lastOrderTime = pesananRepository.findLastOrderTimeByClientId(client.getClientId());

                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(user.getName());
                row.createCell(2).setCellValue(user.getEmail());
                row.createCell(3).setCellValue(user.getPhone() != null ? user.getPhone() : "-");
                row.createCell(4).setCellValue(client.getTotalPoint());
                row.createCell(5).setCellValue(client.getStatusMember().toString());
                row.createCell(6).setCellValue(user.getCreatedAt().toString());
                row.createCell(7).setCellValue(lastOrderTime != null ? lastOrderTime.toString() : "-");
                row.createCell(8).setCellValue(user.isActive() ? "Aktif" : "Non-aktif");
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            log.error("Gagal export data member ke Excel", e);
            throw new RuntimeException("Gagal export data member ke Excel", e);
        }
    }

    private MemberResponse mapToMemberResponse(Client client) {
        User user = client.getUser();
        LocalDateTime lastOrderTime = pesananRepository.findLastOrderTimeByClientId(client.getClientId());
        return new MemberResponse(
                user.getId(),
                client.getClientId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                client.getTotalPoint(),
                user.getCreatedAt(),
                lastOrderTime,
                user.isActive()
        );
    }
}
