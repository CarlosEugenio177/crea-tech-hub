import { useState } from 'react'
import { FormPage } from '../../shared/ui/FormPage.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { QueryStatus } from '../../shared/ui/QueryStatus.tsx'
import { ADMIN_HOME } from '../admin-auth/adminRoutes.ts'
import { OperatingHoursForm } from './OperatingHoursForm.tsx'
import {
  useAdminOperatingHours,
  useUpdateOperatingHours,
} from './operatingHoursApi.ts'

export function OperatingHoursPage() {
  const hours = useAdminOperatingHours()
  const [saved, setSaved] = useState(false)
  const update = useUpdateOperatingHours(() => setSaved(true))

  return (
    <FormPage
      eyebrow="Administração · Funcionamento"
      title="Funcionamento do Hub"
      description="Configure os dias e o intervalo diário usado nas novas reservas."
      backTo={ADMIN_HOME}
      backLabel="Voltar à visão geral"
    >
      {saved && (
        <Notice tone="success">Horário de funcionamento atualizado.</Notice>
      )}
      {hours.data ? (
        <OperatingHoursForm
          hours={hours.data}
          error={update.error}
          isPending={update.isPending}
          onSubmit={(values) => {
            setSaved(false)
            update.submit(values)
          }}
        />
      ) : (
        <QueryStatus error={hours.error} />
      )}
    </FormPage>
  )
}
