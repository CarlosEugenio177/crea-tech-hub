import { useState } from 'react'
import { Button } from '../../shared/ui/Button.tsx'
import { ProblemNotice } from '../../shared/ui/ProblemNotice.tsx'
import { SelectField } from '../../shared/ui/SelectField.tsx'
import { TextAreaField } from '../../shared/ui/TextAreaField.tsx'
import {
  type AdminReservationAction,
  type AdminReservationEntry,
  useDecideAdminReservation,
} from './adminReservationsApi.ts'
import styles from './AdminReservationDecisionForm.module.css'

const actionLabels: Record<AdminReservationAction, string> = {
  CONFIRM: 'Confirmar reserva',
  REJECT: 'Recusar pedido',
  CANCEL: 'Cancelar reserva',
}

export function AdminReservationDecisionForm({
  protocol,
  status,
}: Pick<AdminReservationEntry, 'protocol' | 'status'>) {
  const [selection, setSelection] = useState<{
    action: AdminReservationAction
    status: AdminReservationEntry['status']
  }>()
  const [note, setNote] = useState('')
  const action = selection?.status === status ? selection.action : undefined
  const decision = useDecideAdminReservation(protocol, status, () => {
    setSelection(undefined)
    setNote('')
  })
  const options: AdminReservationAction[] =
    status === 'PENDING' ? ['CONFIRM', 'REJECT', 'CANCEL'] : ['CANCEL']

  return (
    <>
      {status === 'PENDING' || status === 'CONFIRMED' ? (
        <form
          className={styles.form}
          onSubmit={(event) => {
            event.preventDefault()
            if (action && options.includes(action) && !decision.isPending) {
              decision.submit(action, note)
            }
          }}
        >
          <SelectField
            label="Decisão da equipe"
            value={action ?? ''}
            disabled={decision.isPending}
            onChange={(event) => {
              const selected = options.find(
                (option) => option === event.currentTarget.value,
              )
              setSelection(selected ? { action: selected, status } : undefined)
            }}
          >
            <option value="">Selecione uma ação</option>
            {options.map((option) => (
              <option key={option} value={option}>
                {actionLabels[option]}
              </option>
            ))}
          </SelectField>
          {action && options.includes(action) && (
            <>
              <TextAreaField
                label="Mensagem para o solicitante (opcional)"
                hint="A mensagem aparece no link individual do pedido."
                rows={3}
                maxLength={1000}
                value={note}
                disabled={decision.isPending}
                onChange={(event) => setNote(event.currentTarget.value)}
              />
              <Button type="submit" disabled={decision.isPending}>
                {decision.isPending ? 'Salvando…' : actionLabels[action]}
              </Button>
            </>
          )}
        </form>
      ) : null}
      <ProblemNotice error={decision.error} />
    </>
  )
}
