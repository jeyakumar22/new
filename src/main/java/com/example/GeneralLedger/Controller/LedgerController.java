package com.example.GeneralLedger.Controller;

import com.example.GeneralLedger.DTO.Ledger.GeneralLedgerResponseDTO;
import com.example.GeneralLedger.DTO.Ledger.LedgerResponseDTO;
import com.example.GeneralLedger.Service.GlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final GlService glService;

    @GetMapping("/account/{accountCode}")
    public ResponseEntity<LedgerResponseDTO> getAccountLedger(@PathVariable String accountCode) {
        return ResponseEntity.ok(glService.getAccountLedger(accountCode));
    }

    @GetMapping("/general")
    public ResponseEntity<GeneralLedgerResponseDTO> getGeneralLedger() {
        return ResponseEntity.ok(glService.getGeneralLedger());
    }
}
