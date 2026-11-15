import { useOutletContext } from 'react-router'
import type { components } from '../shared/api/schema'
import { PageHeader } from '../shared/ui/PageHeader.tsx'

type AdminAccount = components['schemas']['AdminAccount']

export function AdminHomePage() {
  const account = useOutletContext<AdminAccount>()

  return (
    <PageHeader
      eyebrow="Administração"
      title="Visão geral"
      description={`Você entrou como ${account.fullName}.`}
    />
  )
}
