package com.devsu.bank;

import com.devsu.bank.dto.ClienteRequest;
import com.devsu.bank.dto.ClienteResponse;
import com.devsu.bank.exception.RecursoNoEncontradoException;
import com.devsu.bank.service.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    @Test
    void crearCliente_debeRetornar201() throws Exception {
        ClienteRequest request = new ClienteRequest();
        request.setNombre("Jose Lema");
        request.setIdentificacion("001");
        request.setContrasena("1234");
        request.setEstado(true);

        ClienteResponse response = ClienteResponse.builder()
                .clienteId(1L)
                .nombre("Jose Lema")
                .identificacion("001")
                .estado(true)
                .build();

        when(clienteService.crear(any(ClienteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Jose Lema"))
                .andExpect(jsonPath("$.estado").value(true));
    }

    @Test
    void listarClientes_debeRetornar200ConLista() throws Exception {
        ClienteResponse c1 = ClienteResponse.builder()
                .clienteId(1L).nombre("Jose Lema").estado(true).build();
        ClienteResponse c2 = ClienteResponse.builder()
                .clienteId(2L).nombre("Marianela Montalvo").estado(true).build();

        when(clienteService.listarTodos()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Jose Lema"));
    }

    @Test
    void buscarClientePorId_noExiste_debeRetornar404() throws Exception {
        when(clienteService.buscarPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Cliente", 99L));

        mockMvc.perform(get("/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente con id 99 no encontrado"));
    }

    @Test
    void crearCliente_sinNombre_debeRetornar400() throws Exception {
        ClienteRequest request = new ClienteRequest();
        request.setIdentificacion("001");
        request.setContrasena("1234");
        request.setEstado(true);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
