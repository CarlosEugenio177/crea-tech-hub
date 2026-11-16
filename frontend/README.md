# Frontend do Crea Tech Hub

React com TypeScript e Vite. As convenções estão em [`AGENTS.md`](AGENTS.md), complementando
as [diretrizes da raiz](../AGENTS.md), e a
referência visual em [Design](../docs/09-design.md).

## Executar

Requisitos: Node.js 24.15.0 ou superior na linha 24 (ver `.nvmrc`) e o backend em execução na porta `8080`
(ver [README do backend](../backend/README.md)).

```sh
npm --prefix ../openapi ci --ignore-scripts
npm ci
npm run dev
```

A aplicação abre em `http://localhost:5173`. As chamadas a `/api` são encaminhadas ao
backend pelo servidor de desenvolvimento, na mesma origem. Para outro endereço, defina
`BACKEND_URL` antes de iniciar.

## Verificar

```sh
npm run check
```

Executa, em ordem, sincronização do contrato (`api:check`), formatação (`format:check`), lint (`lint`), tipos (`typecheck`),
testes (`test`) e build (`build`). Cada etapa também pode ser executada separadamente.
Para aplicar a formatação, use `npm run format`.

## Contrato com a API

```sh
npm run api:types
npm run api:check
```

Gera `src/shared/api/schema.d.ts` a partir do contrato oficial `../openapi/admin-api.json`.
Não é necessário iniciar o backend para gerar tipos. Ao mudar a API, atualize o contrato
no backend e gere os tipos na mesma entrega; não edite arquivos gerados nem mantenha
um YAML transcrito à mão. O procedimento completo está em [OpenAPI](../docs/18-openapi.md).
O gerador usa as dependências isoladas de `../openapi/`, instaladas no primeiro comando
de preparação. Assim, o requisito de TypeScript 5 do gerador não altera o TypeScript 7
usado pela aplicação.

## Estrutura

- `src/app`: rotas, layouts e páginas que não pertencem a uma funcionalidade.
- `src/features/<funcionalidade>`: componentes, hooks e chamadas de API de cada funcionalidade.
- `src/shared`: estilos, componentes e recursos usados por mais de uma funcionalidade.
- `src/shared/styles/tokens.css`: único lugar com cores, tipografia e raios.
