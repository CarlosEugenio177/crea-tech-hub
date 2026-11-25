import { useNavigate } from 'react-router'
import { FormPage } from '../../shared/ui/FormPage.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { QueryStatus } from '../../shared/ui/QueryStatus.tsx'
import { SpaceBlockForm } from './SpaceBlockForm.tsx'
import { ADMIN_SPACE_BLOCKS } from './spaceBlockRoutes.ts'
import { useAllAdminSpaces, useCreateSpaceBlock } from './spaceBlocksApi.ts'

export function NewSpaceBlockPage() {
  const navigate = useNavigate()
  const spaces = useAllAdminSpaces()
  const create = useCreateSpaceBlock(() => void navigate(ADMIN_SPACE_BLOCKS))

  return (
    <FormPage
      eyebrow="Administração · Bloqueios"
      title="Bloquear horário"
      description="Defina o espaço, o período local do Hub e o motivo do bloqueio."
      backTo={ADMIN_SPACE_BLOCKS}
      backLabel="Voltar aos bloqueios"
    >
      {spaces.data ? (
        spaces.data.length > 0 ? (
          <SpaceBlockForm
            spaces={spaces.data}
            error={create.error}
            isPending={create.isPending}
            onSubmit={create.submit}
          />
        ) : (
          <Notice tone="info">
            Cadastre um espaço antes de criar um bloqueio de horário.
          </Notice>
        )
      ) : (
        <QueryStatus error={spaces.error} />
      )}
    </FormPage>
  )
}
