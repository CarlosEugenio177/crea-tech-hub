import { useNavigate, useParams } from 'react-router'
import { FormPage } from '../../shared/ui/FormPage.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { QueryStatus } from '../../shared/ui/QueryStatus.tsx'
import { useAdminAccount } from '../admin-auth/adminAccount.ts'
import { PermissionsForm } from './PermissionsForm.tsx'
import { usePermissionOptions, useTeamMember } from './teamApi.ts'
import { ADMIN_TEAM } from './teamRoutes.ts'

export function EditPermissionsPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const account = useAdminAccount()
  const member = useTeamMember(id)
  const options = usePermissionOptions()

  return (
    <FormPage
      eyebrow="Administração · Equipe"
      title="Permissões"
      description="O conjunto marcado substitui as permissões atuais da conta."
      backTo={ADMIN_TEAM}
      backLabel="Voltar para equipe"
    >
      {id === account.id ? (
        <Notice tone="info">
          Você não pode alterar o próprio acesso. Peça a outro gestor.
        </Notice>
      ) : member.data && options.data ? (
        <PermissionsForm
          member={member.data}
          options={options.data}
          onSaved={() => void navigate(ADMIN_TEAM)}
        />
      ) : (
        <QueryStatus error={member.error ?? options.error} />
      )}
    </FormPage>
  )
}
