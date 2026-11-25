import { useState } from 'react'
import { Button } from '../../shared/ui/Button.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { ProblemNotice } from '../../shared/ui/ProblemNotice.tsx'
import styles from './CancelReservationBox.module.css'
import { isLessThan24HoursBeforeStart } from './reservationDates.ts'

type CancelReservationBoxProps = {
  startsAt: string
  lateCancellationWarning: boolean
  isPending: boolean
  error: unknown
  onCancel: (confirmLateCancellation: boolean) => void
}

export function CancelReservationBox({
  startsAt,
  lateCancellationWarning,
  isPending,
  error,
  onCancel,
}: CancelReservationBoxProps) {
  const [confirming, setConfirming] = useState(false)

  if (!confirming) {
    return (
      <Button variant="secondary" onClick={() => setConfirming(true)}>
        Cancelar reserva
      </Button>
    )
  }

  const isLate =
    lateCancellationWarning || isLessThan24HoursBeforeStart(startsAt)

  return (
    <section className={styles.box} aria-label="Confirmação de cancelamento">
      {isLate ? (
        <Notice tone="info">
          Faltam menos de 24 horas para o início. Este cancelamento será
          registrado como tardio.
        </Notice>
      ) : (
        <p className={styles.prompt}>
          Deseja cancelar esta reserva? O horário será liberado imediatamente.
        </p>
      )}
      <ProblemNotice error={error} />
      <div className={styles.actions}>
        <Button disabled={isPending} onClick={() => onCancel(isLate)}>
          {isPending ? 'Cancelando…' : 'Confirmar cancelamento'}
        </Button>
        <Button
          variant="secondary"
          disabled={isPending}
          onClick={() => setConfirming(false)}
        >
          Manter reserva
        </Button>
      </div>
    </section>
  )
}
