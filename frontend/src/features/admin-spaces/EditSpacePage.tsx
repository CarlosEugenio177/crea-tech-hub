import { useNavigate, useParams } from 'react-router'
import { problemMessage } from '../../shared/api/problem'
import { FormPage } from '../../shared/ui/FormPage.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { SpaceForm } from './SpaceForm.tsx'
import { ADMIN_SPACES } from './spaceRoutes.ts'
import { useSpace, useUpdateSpace } from './spacesApi.ts'

export function EditSpacePage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const space = useSpace(id)
  const update = useUpdateSpace(id, () => void navigate(ADMIN_SPACES))

  return (
    <FormPage
      eyebrow="Administração · Espaços"
      title="Editar espaço"
      description="A situação do espaço é alterada na lista de espaços."
      backTo={ADMIN_SPACES}
      backLabel="Voltar para espaços"
    >
      {space.data ? (
        <SpaceForm
          space={space.data}
          submitLabel="Salvar alterações"
          error={update.error}
          isPending={update.isPending}
          onSubmit={update.submit}
        />
      ) : space.isPending ? (
        <Notice tone="info">Carregando…</Notice>
      ) : (
        <Notice tone="error">{problemMessage(space.error)}</Notice>
      )}
    </FormPage>
  )
}
