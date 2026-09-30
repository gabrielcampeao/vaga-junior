package com.gabrielcampeao.posto.web;

import com.gabrielcampeao.posto.domain.Abastecimento;
import com.gabrielcampeao.posto.service.AbastecimentoService;
import com.gabrielcampeao.posto.web.dto.AbastecimentoRequest;
import com.gabrielcampeao.posto.web.dto.AbastecimentoResponse;
import com.gabrielcampeao.posto.web.dto.ErroResponse;
import com.gabrielcampeao.posto.web.dto.ResumoVendasResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Abastecimentos", description = "Endpoints para registro e consulta de abastecimentos")
@RestController
@RequestMapping("/api/abastecimentos")
public class AbastecimentoController {

    private final AbastecimentoService service;

    public AbastecimentoController(AbastecimentoService service) {
        this.service = service;
    }

    @Operation(summary = "Pesquisar abastecimentos com filtro por bomba e período", description = "Retorna lista paginada dos abastecimentos ordenados do mais recente para o mais antigo.")
    @GetMapping
    public Page<AbastecimentoResponse> pesquisar(
            @Parameter(description = "ID da bomba para filtrar") @RequestParam(required = false) Long bombaId,
            @Parameter(description = "Data inicial para o período (ex: 2026-01-01T00:00:00)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data final para o período (ex: 2026-12-31T23:59:59)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            Pageable pageable) {
        return service.pesquisar(bombaId, inicio, fim, pageable).map(AbastecimentoResponse::fromEntity);
    }

    @Operation(summary = "Obter resumo de vendas por período agrupado por tipo de combustível", description = "Retorna o total de litros e valor agrupado por combustível. Sem parâmetros considera os últimos 30 dias; informando apenas fim considera 30 dias anteriores a fim; informando apenas inicio considera de inicio até agora.")
    @GetMapping("/resumo")
    public List<ResumoVendasResponse> resumo(
            @Parameter(description = "Data inicial para o período (ex: 2026-01-01T00:00:00)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data final para o período (ex: 2026-12-31T23:59:59)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return service.obterResumoVendas(inicio, fim);
    }

    @Operation(summary = "Buscar abastecimento por ID", responses = {
            @ApiResponse(responseCode = "200", description = "Abastecimento encontrado"),
            @ApiResponse(responseCode = "404", description = "Abastecimento não encontrado", content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/{id}")
    public AbastecimentoResponse buscarPorId(@PathVariable Long id) {
        return AbastecimentoResponse.fromEntity(service.buscarPorId(id));
    }

    @Operation(summary = "Registrar abastecimento", description = "Informe litros OU valor em reais (nunca ambos). O servidor calcula o campo faltante automaticamente com base no preço por litro da bomba.", responses = {
            @ApiResponse(responseCode = "201", description = "Abastecimento registrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, litros e valor informados juntos, ou limite de litros excedido"),
            @ApiResponse(responseCode = "404", description = "Bomba informada não encontrada", content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @PostMapping
    public ResponseEntity<AbastecimentoResponse> registrar(@Valid @RequestBody AbastecimentoRequest request) {
        Abastecimento salvo = service.registrar(request.bombaId(), request.litros(), request.dataHora(), request.valor());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(salvo.getId())
                .toUri();

        return ResponseEntity.created(location).body(AbastecimentoResponse.fromEntity(salvo));
    }

    @Operation(summary = "Atualizar abastecimento por ID", description = "Informe litros OU valor em reais (nunca ambos).", responses = {
            @ApiResponse(responseCode = "200", description = "Abastecimento atualizado"),
            @ApiResponse(responseCode = "404", description = "Abastecimento ou bomba não encontrada")
    })
    @PutMapping("/{id}")
    public AbastecimentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AbastecimentoRequest request) {
        Abastecimento atualizado = service.atualizar(id, request.bombaId(), request.litros(), request.dataHora(), request.valor());
        return AbastecimentoResponse.fromEntity(atualizado);
    }

    @Operation(summary = "Excluir abastecimento por ID", responses = {
            @ApiResponse(responseCode = "204", description = "Abastecimento excluído"),
            @ApiResponse(responseCode = "404", description = "Abastecimento não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
