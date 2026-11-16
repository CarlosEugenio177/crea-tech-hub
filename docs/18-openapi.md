# Contrato OpenAPI

O backend publica OpenAPI 3.1 com Springdoc 3.2.0, compatível com Spring Boot 4.
O contrato é gerado dos controllers, DTOs e validações, com os complementos de
autenticação e erros em `br.com.mutua.createchhub.openapi`.

O arquivo oficial versionado é [`openapi/admin-api.json`](../openapi/admin-api.json).
[`openapi/schema.d.ts`](../openapi/schema.d.ts) contém os tipos derivados desse arquivo
por `openapi-typescript`. Ambos são gerados; não devem ser editados manualmente.
O frontend pode consumir os arquivos sem iniciar o backend ou um banco local.

## Consultar a documentação local

Na pasta `backend`, com o banco disponível:

```powershell
$env:OPENAPI_ENABLED = 'true'
.\mvnw.cmd spring-boot:run
```

- JSON: `http://localhost:8080/v3/api-docs`
- YAML: `http://localhost:8080/v3/api-docs.yaml`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

A documentação fica desabilitada por padrão. `OPENAPI_ENABLED=true` libera somente
os endpoints de documentação; as APIs administrativas continuam exigindo autenticação
e permissões. Habilite-a conscientemente em ambientes de desenvolvimento.

O Swagger UI permite consultar o contrato. Para testar escritas, preserve o cookie
do GET de CSRF e informe o token atual em **Authorize → csrf**. Após o login, obtenha
outro token. Cookies HttpOnly são gerenciados pelo navegador; não os copie para código
ou armazenamento local.

## Atualizar o contrato

Na pasta `backend`, com Docker em execução:

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress '-Dtest=OpenApiContractTests' '-Dopenapi.update=true' test
```

O teste sobe PostgreSQL temporário, consulta a documentação real e atualiza
`openapi/admin-api.json`. Não depende do banco local nem cria conta administrativa.
Depois, a partir da raiz do repositório, com Node.js 24:

```sh
npm --prefix openapi ci --ignore-scripts
npm --prefix openapi run generate
npm --prefix openapi run check
```

Versione o JSON e os tipos junto com a alteração da API. Em `clean verify`, o teste
compara o contrato publicado com o arquivo versionado e falha se houver diferença.
O JSON consultado também fica em `backend/target/openapi/admin-api.json` para inspeção.
A comparação usa a estrutura JSON, sem depender de finais de linha do sistema.

## Consumir no frontend

- `openapi-typescript` gera os tipos.
- `openapi-fetch` usa esses tipos nas requisições.
- `openapi-react-query` integra o cliente ao React Query.

O contrato provisório da branch do frontend, `frontend/openapi/admin-api.yaml`, deve
ser substituído pelo arquivo oficial acima quando aquela entrega for integrada.
O script `api:types` do frontend deve apontar para ele:

```json
{
  "api:types": "npx --yes openapi-typescript@7.13.0 ../openapi/admin-api.json -o src/shared/api/schema.d.ts"
}
```

Alternativamente, copie `openapi/schema.d.ts` para `frontend/src/shared/api/schema.d.ts`:
os dois arquivos são a mesma saída gerada. Não mantenha um contrato transcrito à mão.
Os nomes públicos dos schemas e os `operationId` preservam os usados pela branch atual
do frontend, evitando que nomes internos de métodos Java alterem os tipos sem intenção.

O cliente HTTP compartilhado continua responsável por cookie, CSRF e `ProblemDetail`.
Os tipos gerados verificam o código TypeScript; validações no backend continuam obrigatórias.
Alterações incompatíveis precisam ser coordenadas com os consumidores antes da integração.

## Regras para novas operações

- Defina um `operationId` único e estável com `@Operation`.
- Use DTOs explícitos, Bean Validation e respostas com o status real. Respostas dinâmicas
  em `ResponseEntity`, como criação e operações sem corpo, precisam declarar seu status
  para o gerador, mantendo-o igual ao comportamento existente.
- Documente campos anuláveis e campos obrigatórios das respostas. Nunca exporte entidades
  JPA ou modelos internos de sessão, senha ou persistência.
- Preserve os nomes públicos dos schemas com `@Schema` quando o nome Java for diferente.
- Complete autenticação, CSRF e erros no customizador quando surgir uma nova exceção ao
  fluxo padrão. Cookie e CSRF aparecem como requisitos conjuntos nas escritas autenticadas.
- Login e logout são documentados explicitamente porque são tratados pelos filtros do Spring
  Security. Login recebe formulário `application/x-www-form-urlencoded`, não JSON.
- Todas as operações têm erro padrão `application/problem+json`, incluindo os erros de campos.

## CI

O job de testes verifica o contrato gerado e a sincronização dos tipos TypeScript,
usando a versão fixada e o lockfile de `openapi/`. Quando `frontend/package-lock.json`
estiver presente, também instala o frontend, aplica os tipos oficiais, executa a checagem
TypeScript e exige que o arquivo gerado já esteja versionado corretamente. Isso impede
integrar uma API com tipos ou consumidores desatualizados.

Referências: [Springdoc](https://springdoc.org/),
[openapi-typescript](https://openapi-ts.dev/introduction) e
[openapi-react-query](https://openapi-ts.dev/openapi-react-query/).
