# Integração contínua e entrega do backend

## Fluxo de branches

`main` é a branch principal e representa a versão destinada à produção.
`develop` reúne as entregas em desenvolvimento para integração e validação da equipe.

Os desenvolvedores trabalham em branches próprias e enviam pull requests para
`develop`. Depois da validação, a promoção de `develop` para `main` é feita por um
pull request revisado e aprovado pelo responsável pelo projeto.

Após a integração de uma entrega, a branch de trabalho pode ser removida. As branches
permanentes são `main` e `develop`.

## CI no GitHub Actions

O workflow [Backend CI](../.github/workflows/backend-ci.yml) executa em pushes e pull
requests destinados a `develop` ou `main`. Cada novo commit enviado para a branch de
um pull request dispara uma nova execução dos checks.

Em pull requests, os checks são separados em:

1. **Formatação:** verifica o Spotless com `spotless:check`.
2. **Análise estática:** executa o SpotBugs.
3. **Testes e cobertura:** executa `clean verify`, incluindo os testes com Testcontainers
   e o limite de cobertura do JaCoCo.
4. **Build:** gera o JAR somente depois que os três checks anteriores passarem.

Todos os jobs obtêm o código e preparam Java 25 (Temurin), com cache das dependências
Maven. O build disponibiliza o JAR executável como artefato e o job de testes disponibiliza
os relatórios, inclusive quando os testes falham.

O JAR é identificado pelo SHA da revisão construída. Em pull requests, o GitHub valida
o commit de merge de teste com a branch de destino. Os artefatos ficam disponíveis por
7 dias na página da execução, na aba **Actions** do repositório.

Para impedir o merge quando um check falhar, os checks devem ser marcados como obrigatórios
nas regras de proteção da branch `develop` no GitHub. A execução do workflow, por si só,
não bloqueia o merge.

Novos commits cancelam a execução anterior do mesmo pull request ou branch. As actions
são fixadas por SHA e o workflow usa permissão de leitura do repositório.

A CI não precisa de secrets nem conta de hospedagem nesta etapa. Os testes usam
Docker, disponível no runner Ubuntu, para criar bancos PostgreSQL temporários com
Testcontainers.

## CD: destino a definir

A hospedagem ainda será escolhida. Esta entrega configura a validação e a geração do
artefato; o deploy automático será conectado depois da definição do ambiente.

Para configurar o CD, será necessário definir o destino de desenvolvimento, a forma
de autenticação, as variáveis da aplicação e como verificar a saúde após o deploy.
O fluxo previsto é publicar no ambiente de desenvolvimento após a integração em
`develop`, usando um artefato aprovado pela CI. Credenciais serão armazenadas como
secrets do ambiente/repositório, conforme a hospedagem escolhida.
