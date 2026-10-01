package com.gabrielcampeao.posto.web.error;

import com.gabrielcampeao.posto.domain.Bomba;
import com.gabrielcampeao.posto.web.dto.ErroResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TratadorGlobalExcecoesTest {

    @Test
    @DisplayName("Deve tratar ObjectOptimisticLockingFailureException retornando 409 com mensagem de conflito")
    void deveTratarExclusaoConcorrenteComo409() {
        TratadorGlobalExcecoes tratador = new TratadorGlobalExcecoes();
        ObjectOptimisticLockingFailureException ex = new ObjectOptimisticLockingFailureException(Bomba.class, 1L);

        ResponseEntity<ErroResponse> response = tratador.handleExclusaoConcorrente(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflito", response.getBody().erro());
        assertEquals("O recurso foi alterado ou removido por outra requisição; tente novamente", response.getBody().mensagem());
    }

    @Test
    @DisplayName("Deve tratar ConstraintViolationException retornando 400 com mapa de campos")
    void deveTratarConstraintViolationExceptionComo400() {
        TratadorGlobalExcecoes tratador = new TratadorGlobalExcecoes();

        jakarta.validation.Path path = org.mockito.Mockito.mock(jakarta.validation.Path.class);
        org.mockito.Mockito.when(path.toString()).thenReturn("valorTotal");

        jakarta.validation.ConstraintViolation<?> violation = org.mockito.Mockito.mock(jakarta.validation.ConstraintViolation.class);
        org.mockito.Mockito.when(violation.getPropertyPath()).thenReturn(path);
        org.mockito.Mockito.when(violation.getMessage()).thenReturn("deve ser maior que 0");

        jakarta.validation.ConstraintViolationException ex = new jakarta.validation.ConstraintViolationException(
                "Validation failed", java.util.Set.of(violation));

        ResponseEntity<ErroResponse> response = tratador.handleConstraintViolation(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Validação", response.getBody().erro());
        assertNotNull(response.getBody().campos());
        assertEquals("deve ser maior que 0", response.getBody().campos().get("valorTotal"));
    }
}
