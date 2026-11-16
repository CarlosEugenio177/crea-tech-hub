# Diretrizes do Crea Tech Hub

## Escopo e referências

- Monorepo de uma aplicação web. O backend fica em `backend/`: Java 25, Spring Boot, Maven Wrapper, PostgreSQL, JPA e Flyway. O frontend fica em `frontend/`: React, TypeScript e Vite. Implementar em etapas pequenas.
- Consultar `histories/PLANO-DE-IMPLEMENTACAO.md`, a história correspondente e `docs/12-ci-cd.md` a `docs/17-equipe-permissoes-recuperacao.md` antes de alterar comportamento.
- As decisões mais recentes do responsável prevalecem sobre o levantamento original. Ao mudar uma regra, atualizar a história/documentação afetada.
- O Framer orienta a identidade visual. Telas demonstrativas não definem regras de negócio nem comprovam funcionalidade implementada. Ver `docs/09-design.md`.
- Hospedagem e ambientes de deploy ainda estão a definir. Não inventar infraestrutura, CD ou integrações externas.

## Simplicidade e organização

- Seguir KISS e YAGNI: resolver a necessidade atual com a solução mais simples que preserve correção e manutenção.
- Aplicar SOLID com bom senso. Favorecer composição e responsabilidades pequenas; criar pontos de extensão quando houver uma necessidade concreta.
- Não criar monster classes, serviços que concentrem todo o sistema ou superclasses genéricas de controller, serviço e entidade.
- Não criar interfaces com uma única implementação, pares `Service`/`ServiceImpl`, factories ou padrões apenas por convenção. Uma integração que precise de isolamento pode justificar uma interface.
- Evitar abstrações prematuras. Extrair duplicação quando representar a mesma regra; semelhança de código sozinha não justifica uma abstração.
- Organizar por funcionalidade, seguindo os pacotes existentes. Usar `shared` apenas para conceitos realmente compartilhados.
- Controllers cuidam de HTTP; serviços coordenam casos de uso e transações; entidades preservam suas invariantes; repositórios cuidam da persistência.
- Preferir injeção por construtor, nomes claros e métodos coesos. Não fragmentar código em classes sem responsabilidade própria para cumprir limites arbitrários de linhas.
- Usar DTOs explícitos nos contratos HTTP; records quando apropriado. Não serializar entidades JPA nem expor hashes ou campos internos.
- Reaproveitar Spring e Java antes de adicionar dependências. Não introduzir ferramentas, frameworks ou versões novas sem necessidade da entrega.

## Comentários e estilo

- Não adicionar comentários desnecessários, narração do código, marcadores decorativos ou código comentado.
- Comentar somente razões, restrições ou decisões que não sejam evidentes pelo código. Registrar explicações extensas na documentação.
- Preservar os nomes de domínio e convenções existentes. Código Java em inglês; documentação e mensagens ao usuário em português.
- A formatação Java é definida pelo Spotless/Google Java Format, com dois espaços. Respeitar `.editorconfig` nos demais arquivos.
- Manter mudanças focadas; evitar refatorações e reformatação de arquivos sem relação com a tarefa.

## Regras já decididas

- Nesta fase, o solicitante usa o fluxo público sem criar conta. Seus dados pertencem à solicitação; não implementar cadastro obrigatório, CPF obrigatório ou validação Crea fictícia.
- Login local exclusivo de administradores ativos, com e-mail único normalizado e senha. A autenticação futura do solicitante via Crea/Mútua ainda depende de definição.
- As roles são `ADMIN` e `USER`. Mutualista, profissional do Crea e outros vínculos são informações independentes das permissões. Não conceder benefícios por vínculo autodeclarado.
- Superadministrador é um `ADMIN` com `MANAGE_ACCESS`, sem nova role. Novas ações administrativas exigem permissão explícita no backend; não presumir acesso total pela role.
- Gestão de equipe é restrita a `MANAGE_ACCESS`. Preservar a proteção do último gestor, a proibição de alterar o próprio acesso e o bloqueio transacional das mudanças. Mudanças de senha/permissões/situação devem invalidar sessões anteriores e ser auditadas sem segredos.
- Toda solicitação passa por análise manual. `PENDING` e `CONFIRMED` bloqueiam o recurso no intervalo solicitado; pendências não expiram automaticamente.
- O solicitante informa início e fim com precisão de minutos. Não impor slots fixos. Intervalos são `[início, fim)`: uma reserva pode começar quando a anterior termina.
- Ao implementar reservas, garantir ausência de sobreposição também sob concorrência, com proteção transacional no PostgreSQL; uma consulta prévia isolada não basta.
- O envio gera comprovante de solicitação pendente. Confirmar somente após decisão administrativa; nunca substituir automaticamente uma reserva por prioridade interna.
- Acompanhamento e cancelamento públicos usarão link individual imprevisível. O protocolo sozinho não autoriza acesso. Não expor listagem pública de reservas ou dados pessoais.
- Cancelamento é permitido a qualquer momento; antecedência inferior a 24 horas deve ser registrada. Sanções automáticas, prioridade, cobrança, relatórios e integrações futuras não devem ser antecipados.
- Antecedência mínima, limites por ambiente e demais pontos pendentes não devem ser inventados. Consultar a história e esclarecer somente o que bloquear a entrega.

## Validação, persistência e segurança

- Validar entradas com Bean Validation e preservar invariantes de domínio e restrições no banco. `@Email` verifica formato; não comprova titularidade ou existência da caixa postal.
- Aplicar limites, paginação e ordenação previsível em consultas de listagem conforme forem criadas. Evitar N+1 e carregamento de relações desnecessárias.
- Manter `open-in-view: false` e `ddl-auto: validate`. Alterações de esquema entram em novas migrações Flyway; não editar migrações já aplicadas nem apagar dados para contornar erros.
- Usar transações nos casos de uso que alteram dados. Não chamar serviços externos segurando transações de banco sem justificativa.
- Centralizar erros HTTP em `shared.error`, com `ProblemDetail`. Erros esperados têm status apropriado; falhas internas geram logs no servidor e resposta sanitizada.
- Não devolver stack traces, SQL, credenciais, hashes ou valores pessoais rejeitados nas respostas de erro. Não registrar senhas, tokens ou dados pessoais desnecessários nos logs.
- Preservar as proteções de sessão, CSRF e autorização existentes. Liberar novas rotas públicas explicitamente conforme a funcionalidade for entregue.
- Não criar senha padrão, administrador automático no perfil normal ou credenciais versionadas. O primeiro administrador usa somente o bootstrap explícito documentado.
- Configuração vem de variáveis de ambiente. `.env` é local e ignorado; `.env.example` documenta nomes sem segredos. HTTPS deve usar cookie seguro.

## Frontend

- O frontend fica em `frontend/`: React com TypeScript em modo estrito, Vite, React Router e CSS Modules. As regras de simplicidade, estilo, segurança e Git deste documento valem também para ele.
- Bibliotecas já aprovadas, adicionadas somente quando a primeira funcionalidade precisar delas: TanStack Query com `openapi-react-query` para dados do servidor, React Hook Form com Zod para formulários. Testes usam Vitest e Testing Library; lint usa Oxlint e a formatação, Prettier.
- O contrato com a API vem do OpenAPI do backend: tipos gerados com `openapi-typescript` e chamadas feitas com `openapi-fetch`. Não escrever à mão tipos de requisição ou resposta, nem editar arquivos gerados; ao mudar a API, regenerar os tipos na mesma entrega. Enquanto o backend não publicar o OpenAPI, a fonte é `frontend/openapi/admin-api.yaml`, transcrita dos controllers.
- Um único cliente HTTP compartilhado cuida de cookie de sessão, CSRF e leitura de `ProblemDetail`. Componentes não chamam `fetch` diretamente.
- A integração com a API oficial é incremental e acompanha a evolução do backend: cada tela passa a usar a API na entrega em que o endpoint correspondente existir, estendendo o contrato OpenAPI e regenerando os tipos. Enquanto o endpoint não existir, a tela fica navegável, não envia dados nem simula respostas, e avisa o usuário do que ainda não está disponível. Dados fixos provisórios ficam isolados em um arquivo da funcionalidade, com comentário indicando o que os substitui.
- Situação em 16/11/2026: login, espaços e equipe usam a API. Aguardam endpoints: catálogo público (`features/reservation/spaceOptions.ts` é provisório), envio da solicitação, comprovante, acompanhamento por link individual e painel de reservas. Atualizar este item a cada integração.
- Organizar por funcionalidade, espelhando o backend: `src/features/<funcionalidade>/` reúne componentes, hooks e chamadas de API daquela funcionalidade. `src/shared/` recebe apenas o que for usado por mais de uma funcionalidade.
- Componentes pequenos, com uma responsabilidade e um componente por arquivo. Dividir o arquivo quando ele acumular responsabilidades; não criar arquivos monstruosos nem fragmentar sem motivo.
- Separar lógica de apresentação: busca de dados, estado e regras ficam em hooks ou funções; componentes de tela compõem componentes menores e recebem dados por props.
- Preferir estado local e composição. Não adicionar estado global, bibliotecas ou camadas de abstração sem uma necessidade concreta da entrega.
- Não usar `any` nem silenciar o compilador ou o lint para fazer o código passar. Nomes claros dispensam comentários.
- Estilos em CSS Modules, um arquivo por componente. Cores, tipografia e raios vêm dos tokens de `docs/09-design.md`, definidos somente em `src/shared/styles/tokens.css`. Não espalhar valores visuais soltos pelos componentes.
- Toda tela deve funcionar em celular e computador, com contraste adequado, navegação por teclado e textos alternativos, conforme CTH-21.
- A interface pode ocultar o que a conta não pode fazer, mas a autorização é sempre do backend. Não guardar senhas ou tokens em `localStorage`, logs ou URLs.
- Não deixar arquivos Markdown soltos pelo projeto. Documentação fica em `docs/`, diretrizes neste arquivo, e `frontend/README.md` explica apenas como executar e verificar.

## Verificação

- Testar comportamento relevante: regras, validação, permissões, erros, migrações e conflitos. Não criar testes triviais de getters ou testes que apenas espelhem a implementação.
- Testes de persistência e integração usam PostgreSQL com Testcontainers e Docker; não substituir por H2 para esconder incompatibilidades.
- Na pasta `backend`, executar os checks pertinentes à mudança:
  - Windows: `.\mvnw.cmd --batch-mode --no-transfer-progress spotless:check`
  - Windows: `.\mvnw.cmd --batch-mode --no-transfer-progress clean verify`
  - Windows: `.\mvnw.cmd --batch-mode --no-transfer-progress compile spotbugs:spotbugs spotbugs:check -DskipTests`
  - Linux/devcontainer: substituir `.\mvnw.cmd` por `./mvnw`.
- Na pasta `frontend`, executar `npm run check` antes de abrir o PR: formatação, lint, tipos, testes e build.
- Para aplicar o formatador, usar `spotless:apply`. Não reduzir cobertura, desativar verificações ou ampliar exclusões para fazer a CI passar.
- Validar alterações de Compose com `docker compose config --quiet`, sem imprimir credenciais. Mudanças em geradores de protótipo exigem regenerar e conferir os artefatos afetados.
- Informar quais checks passaram e o que não pôde ser executado. Falha de faturamento do GitHub Actions exige ajuste da conta; não é corrigida alterando o código.

## Git e colaboração

- Desenvolver em branch de trabalho e abrir PR para `develop`. A promoção é `develop` → PR revisado pelo responsável → `main`, destinada à produção.
- Não enviar mudanças diretamente para `main` nem promover para produção sem autorização. Não apagar branches alheias, forçar push, descartar mudanças locais ou reescrever histórico sem necessidade e autorização.
- Conferir `git status`, branches remotas e alterações de colegas antes de trabalhar. Preservar e integrar o trabalho existente.
- Commits e PRs devem explicar o problema, a mudança e a validação. Não declarar deploy, aprovação ou testes que não aconteceram.
- Comunicar de forma breve, em português, destacando resultado e impedimentos concretos. Perguntar quando faltar uma decisão de negócio indispensável; prosseguir autonomamente no que já estiver definido.
