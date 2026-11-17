import { useNavigate } from 'react-router'
import { FormPage } from '../../shared/ui/FormPage.tsx'
import { QueryStatus } from '../../shared/ui/QueryStatus.tsx'
import { MemberForm } from './MemberForm.tsx'
import { usePermissionOptions } from './teamApi.ts'
import { ADMIN_TEAM } from './teamRoutes.ts'

export function NewMemberPage() {
  const navigate = useNavigate()
  const options = usePermissionOptions()

  return (
    <FormPage
      eyebrow="Administração · Equipe"
      title="Novo funcionário"
      description="A conta é criada ativa e o funcionário define a senha pessoal no primeiro acesso."
      backTo={ADMIN_TEAM}
      backLabel="Voltar para equipe"
    >
      {options.data ? (
        <MemberForm
          options={options.data}
          onSaved={() => void navigate(ADMIN_TEAM)}
        />
      ) : (
        <QueryStatus error={options.error} />
      )}
    </FormPage>
  )
}
