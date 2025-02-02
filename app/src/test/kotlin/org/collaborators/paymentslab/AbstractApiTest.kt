package org.collaborators.paymentslab

import com.fasterxml.jackson.databind.ObjectMapper
import org.collaborator.paymentlab.common.URI_HOST
import org.collaborator.paymentlab.common.URI_PORT
import org.collaborator.paymentlab.common.URI_SCHEME
import org.collaborators.paymentslab.account.domain.AccountRepository
import org.collaborators.paymentslab.account.domain.PasswordEncrypt
import org.collaborators.paymentslab.account.domain.TokenGenerator
import org.collaborators.paymentslab.payment.domain.repository.PaymentHistoryRepository
import org.collaborators.paymentslab.payment.domain.repository.PaymentOrderRepository
import org.collaborators.paymentslab.payment.infrastructure.tosspayments.PaymentPropertiesResolver
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.*
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = URI_SCHEME, uriHost = URI_HOST, uriPort = URI_PORT)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(RestDocumentationExtension::class)
@ActiveProfiles("test")
abstract class AbstractApiTest {

    @Value("\${uri.scheme}")
    protected lateinit var scheme: String

    @Value("\${uri.host}")
    protected lateinit var host: String

    @Value("\${uri.port}")
    protected lateinit var port: String

    @Value("\${admin.key}")
    protected lateinit var adminKey: String

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var tokenGenerator: TokenGenerator

    @Autowired lateinit var encrypt: PasswordEncrypt

    @MockBean
    protected lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @MockBean
    protected lateinit var accountRepository: AccountRepository

    @MockBean
    protected lateinit var paymentHistoryRepository: PaymentHistoryRepository

    @MockBean
    protected lateinit var paymentOrderRepository: PaymentOrderRepository

    protected val objectMapper: ObjectMapper = ObjectMapper()

    @Autowired
    protected lateinit var paymentPropertiesResolver: PaymentPropertiesResolver

    protected fun getDocumentRequest(): OperationRequestPreprocessor {
        return Preprocessors.preprocessRequest(
            Preprocessors.modifyUris()
                .scheme(scheme)
                .host(host)
                .port(port.toInt()),
            Preprocessors.prettyPrint()
        )
    }

    protected fun errorResponseFieldsSnippet(): ResponseFieldsSnippet? = PayloadDocumentation.responseFields(
        PayloadDocumentation.fieldWithPath("isSuccess")
            .type(JsonFieldType.BOOLEAN)
            .description("api 성공여부"),
        PayloadDocumentation.fieldWithPath("body.code")
            .type(JsonFieldType.STRING)
            .description("오류 코드(!= HTTP STATUS CODE)"),
        PayloadDocumentation.fieldWithPath("body.message")
            .type(JsonFieldType.STRING)
            .description("오류 메세지 내용")
    )
}