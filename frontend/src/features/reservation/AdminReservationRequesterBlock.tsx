import { useState } from 'react'
import { Button } from '../../shared/ui/Button.tsx'
import { ProblemNotice } from '../../shared/ui/ProblemNotice.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import styles from './AdminReservationRequesterBlock.module.css'
import { useAdminRequesterBlock } from './adminRequesterBlockApi.ts'
import { formatHubInstant } from './reservationDates.ts'

type Props = {
  protocol: string
  count: number
  blockedUntil?: string
}

export function AdminReservationRequesterBlock({
  protocol,
  count,
  blockedUntil,
}: Props) {
  const [days, setDays] = useState('')
  const block = useAdminRequesterBlock(protocol, () => setDays(''))
  const duration = Number(days)
  const valid =
    Number.isInteger(duration) && duration > 0 && duration <= 2_147_483_647

  return (
    <section className={styles.section} aria-label="Bloqueio por faltas">
      <p>{count} faltas registradas</p>
      {blockedUntil && <p>Bloqueado até {formatHubInstant(blockedUntil)}.</p>}
      <form
        className={styles.form}
        onSubmit={(event) => {
          event.preventDefault()
          if (valid && !block.isPending) block.submit(duration)
        }}
      >
        <TextField
          label="Duração do bloqueio em dias"
          type="number"
          min={1}
          step={1}
          inputMode="numeric"
          value={days}
          onChange={(event) => setDays(event.currentTarget.value)}
          disabled={block.isPending}
        />
        <Button type="submit" disabled={!valid || block.isPending}>
          {blockedUntil ? 'Atualizar bloqueio' : 'Bloquear novas reservas'}
        </Button>
      </form>
      {blockedUntil && (
        <Button
          variant="secondary"
          disabled={block.isPending}
          onClick={block.revoke}
        >
          Remover bloqueio
        </Button>
      )}
      <ProblemNotice error={block.error} />
    </section>
  )
}
