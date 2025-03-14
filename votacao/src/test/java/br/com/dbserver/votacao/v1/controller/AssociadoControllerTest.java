package br.com.dbserver.votacao.v1.controller;

import br.com.dbserver.votacao.clientmock.CpfClientMock;
import br.com.dbserver.votacao.rabbitmq.config.RabbitMQConection;
import br.com.dbserver.votacao.v1.dto.request.AssociadoRequest;
import br.com.dbserver.votacao.v1.dto.response.AssociadoResponse;
import br.com.dbserver.votacao.v1.enums.StatusUsuarioEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import static br.com.dbserver.votacao.SqlProvider.insertAssociado;
import static br.com.dbserver.votacao.SqlProvider.resetarDB;
import static br.com.dbserver.votacao.stubs.AssociadoStub.construirAssociadoRequest;
import static br.com.dbserver.votacao.stubs.AssociadoStub.construirAssociadoResponse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Associado Controller")
class AssociadoControllerTest extends CpfClientMock {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private final ObjectMapper mapper = new ObjectMapper();

	@MockBean
	RabbitMQConection rabbitMQConection;

	private String retornoComoJson;
	private String envioComoJSON;
	AssociadoRequest associadoRequest;

	@BeforeEach
	public void inicializar() throws JsonProcessingException {
		associadoRequest = construirAssociadoRequest(StatusUsuarioEnum.PODE_VOTAR);
		envioComoJSON = mapper.writeValueAsString(associadoRequest);
		AssociadoResponse associadoResponse = construirAssociadoResponse(StatusUsuarioEnum.PODE_VOTAR);
		retornoComoJson = mapper.writeValueAsString(associadoResponse);
		this.mockCpfClientValidarCpf();
	}

	@Test
	@DisplayName("Teste POST/SUCESSO salvar um Associado")
	@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = resetarDB)
	void salvarAssociado() throws Exception {
		mockMvc.perform(post("/v1/associado")
						.content(envioComoJSON)
						.accept(MediaType.APPLICATION_JSON_VALUE)
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json(retornoComoJson))
				.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("POST/EROO salvar um Associado com documento invalido")
	void salvarAssociadoError() throws Exception {
		associadoRequest.setDocumento("12345");
		envioComoJSON = mapper.writeValueAsString(associadoRequest);

		mockMvc.perform(post("/v1/associado")
						.content(envioComoJSON)
						.accept(MediaType.APPLICATION_JSON_VALUE)
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest());
	}

	@Test
	@SqlGroup({
			@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = insertAssociado),
			@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = resetarDB)
	})
	@DisplayName("GET/SUCESSO buscar um Associado")
	void buscarAssociadoPorCpfOuCnpj() throws Exception {
		mockMvc.perform(get("/v1/associado/{cpfOuCnpj}", "90015955028")
						.content(envioComoJSON)
						.accept(MediaType.APPLICATION_JSON_VALUE)
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json(retornoComoJson))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("GET/Error ao buscar associado inexistente")
	public void testeGetDocumentoInvalido() throws Exception {
		mockMvc.perform(get("/v1/associado/{cpfOuCnpj}", "90015955028")
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNotFound());
	}
}