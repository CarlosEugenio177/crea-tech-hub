import { useNavigate } from 'react-router'
import { FormPage } from '../../shared/ui/FormPage.tsx'
import { SpaceForm } from './SpaceForm.tsx'
import { ADMIN_SPACES } from './spaceRoutes.ts'
import { useCreateSpace } from './spacesApi.ts'

export function NewSpacePage() {
  const navigate = useNavigate()
  const create = useCreateSpace(() => void navigate(ADMIN_SPACES))

  return (
    <FormPage
      eyebrow="Administração · Espaços"
      title="Novo espaço"
      description="O espaço é cadastrado como ativo e pode ser desativado depois."
      backTo={ADMIN_SPACES}
      backLabel="Voltar para espaços"
    >
      <SpaceForm
        submitLabel="Cadastrar espaço"
        error={create.error}
        isPending={create.isPending}
        onSubmit={create.submit}
      />
    </FormPage>
  )
}
