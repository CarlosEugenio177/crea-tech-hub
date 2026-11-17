# Diretrizes do frontend

Estas regras complementam o `AGENTS.md` da raiz. Usar React, TypeScript estrito,
Vite, React Router e as bibliotecas já aprovadas; preservar KISS e YAGNI.

## Contrato e cliente HTTP

- A única fonte do contrato é `../openapi/admin-api.json`, publicado pelo backend.
  Não criar YAML paralelo nem inferir contratos a partir de exemplos ou telas.
- Gerar `src/shared/api/schema.d.ts` com `npm run api:types`. Não editar esse arquivo
  manualmente nem formatá-lo com Prettier. `npm run api:check` verifica a sincronização.
- O gerador é instalado somente em `../openapi/`, com `npm --prefix ../openapi ci --ignore-scripts`.
  Não adicioná-lo às dependências do frontend nem contornar seus peers com `--force`
  ou `--legacy-peer-deps`; ele exige TypeScript 5 e a aplicação usa TypeScript 7.
- Usar `components` e `paths` gerados para requisições, respostas, enums e parâmetros.
  Não duplicar DTOs, usar `any` ou adicionar casts para esconder incompatibilidades.
- `src/shared/api/client.ts` centraliza `openapi-fetch`, `openapi-react-query`, cookie
  de sessão e CSRF. Componentes não fazem chamadas diretas a `fetch`.
- Obter CSRF antes das escritas, incluindo login/logout e recuperação. Após o login,
  obter o token da nova sessão. Login envia formulário, conforme o contrato.
- Tratar `ProblemDetail` pelo código compartilhado; diferenciar 401, 403 e erros de
  campo. Não mostrar stack traces, SQL ou mensagens internas para o usuário.
- A documentação HTTP e Swagger são opcionais. Os arquivos oficiais versionados
  permitem gerar tipos sem backend ou PostgreSQL em execução.

## Organização e permissões

- Manter código por funcionalidade em `src/features/`; `src/shared/` recebe somente
  código realmente compartilhado. Separar apresentação, chamadas e estado do servidor.
- Componentes pequenos e coesos, sem componentes que concentrem todo o painel,
  superclasses ou abstrações especulativas. Comentários explicam somente decisões
  que não fiquem claras pelo código.
- Cada funcionalidade concentra suas chamadas em `<funcionalidade>Api.ts`. Os hooks de
  escrita devolvem `{ error, isPending, submit }` (ou um verbo equivalente, como `change`)
  com o corpo tipado pelo contrato; componentes não montam `body`, `params` ou cabeçalhos.
- Erros da API aparecem com `ProblemNotice`; carregamento e falha de consultas, com
  `QueryStatus`. Campos de formulário usam `Field` (via `TextField`, `SelectField` e
  `TextAreaField`), que liga rótulo, dica, erro e atributos de acessibilidade.
- Datas, horários e números seguem o padrão brasileiro: exibição com `Intl` em `pt-BR`
  e entrada em `dd/mm/aaaa` e `hh:mm` (24 h), com máscara. Não usar `type="date"` ou
  `type="time"`, que seguem o idioma do navegador. Valores enviados à API usam ISO.
- A conta autenticada no painel vem de `useAdminAccount()`. Importações relativas
  usam a extensão do arquivo (`.ts`/`.tsx`), exceto os tipos gerados em `schema`.
- O painel usa `/admin`, com layout e navegação próprios. Funcionalidades públicas
  e administrativas compartilham o frontend, preservando a separação dos fluxos.
- As roles continuam `ADMIN` e `USER`; superadministrador é um `ADMIN` com
  `MANAGE_ACCESS`. Usar as permissões de `me` para montar menus e ações.
- A autorização é sempre do backend. Em 401, descartar o estado privado da sessão;
  mudanças de acesso podem invalidar sessões e exigir novo login.
- Respeitar `passwordChangeRequired`: orientar a troca inicial antes de usar o painel.
  Não armazenar senha, sessão ou token de recuperação em `localStorage` ou logs.
- Usar React Hook Form e Zod para validar formulários quando necessário; a validação
  do frontend ajuda o usuário e não substitui a validação do backend.
- Estender o fluxo real somente quando houver endpoint correspondente. Telas futuras
  não devem simular sucesso, envio de mensagens ou persistência.
- Seguir tokens visuais existentes, acessibilidade, navegação por teclado e layouts
  responsivos. Não adicionar dependências ou trocar a arquitetura sem necessidade.
- Usar textos curtos e concretos, ligados à ação ou ao estado da tela. Rótulos como
  "Bloquear horário" e "Cancelar bloqueio" devem dizer o que acontece. Evitar slogans,
  adjetivos vazios e explicações repetidas nos formulários e avisos.
- O editor de salas usa as APIs e versões descritas em `../docs/22-montagem-de-salas.md`.
  Arrastar altera um rascunho; gravar exige ação explícita. Preservar mudanças locais
  em falhas de rede e conflitos, oferecer operação pelo teclado e manter os UUIDs.
  Medidas e remoções entram no mesmo rascunho; selecionar várias permite alinhar,
  distribuir e remover com desfazer antes de salvar. Preservar o encaixe opcional,
  as guias e a seleção por número para quem usa toque ou teclado.

## Verificação e entrega

- Usar Node.js 24.15.0 ou superior na linha 24, conforme `.nvmrc` e `engines`, e `npm ci`.
- Ao mudar o backend, regenerar primeiro o contrato oficial conforme `../docs/18-openapi.md`,
  depois executar `npm run api:types` no frontend e versionar os tipos na mesma entrega.
- Executar `npm run check`: contrato, formatação, lint, TypeScript, testes e build.
  Corrigir as incompatibilidades; não remover verificações para aprovar a entrega.
- Testar comportamento do usuário, erros e permissões. Não criar testes triviais
  de implementação nem repetir a suíte depois de passar sem mudanças relevantes.
- Trabalhar em branch e PR para `develop`, preservando mudanças dos colegas.
  Documentação geral fica em `../docs/`; o README local explica execução e verificação.
