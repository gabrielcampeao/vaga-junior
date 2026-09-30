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
}
