# Frontend do Crea Tech Hub

React com TypeScript e Vite. As convenções estão em [`AGENTS.md`](../AGENTS.md) e a
referência visual em [Design](../docs/09-design.md).

## Executar

Requisitos: Node.js 24 e o backend em execução na porta `8080`
(ver [README do backend](../backend/README.md)).

```sh
npm install
npm run dev
```

A aplicação abre em `http://localhost:5173`. As chamadas a `/api` são encaminhadas ao
backend pelo servidor de desenvolvimento, na mesma origem. Para outro endereço, defina
`BACKEND_URL` antes de iniciar.

## Verificar

```sh
npm run check
```

Executa, em ordem, formatação (`format:check`), lint (`lint`), tipos (`typecheck`),
testes (`test`) e build (`build`). Cada etapa também pode ser executada separadamente.
Para aplicar a formatação, use `npm run format`.

## Contrato com a API

```sh
npm run api:types
```

Gera `src/shared/api/schema.d.ts` a partir de `openapi/admin-api.yaml`. Esse documento é
provisório, transcrito dos controllers do backend, e deve dar lugar ao OpenAPI publicado
pelo backend. Ao mudar a API, atualize o documento e gere os tipos na mesma entrega.

## Estrutura

- `src/app`: rotas, layouts e páginas que não pertencem a uma funcionalidade.
- `src/features/<funcionalidade>`: componentes, hooks e chamadas de API de cada funcionalidade.
- `src/shared`: estilos, componentes e recursos usados por mais de uma funcionalidade.
- `src/shared/styles/tokens.css`: único lugar com cores, tipografia e raios.
