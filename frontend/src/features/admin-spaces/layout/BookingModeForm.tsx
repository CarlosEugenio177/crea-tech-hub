import { useState } from 'react'
import { Button } from '../../../shared/ui/Button.tsx'
import { ProblemNotice } from '../../../shared/ui/ProblemNotice.tsx'
import { SelectField } from '../../../shared/ui/SelectField.tsx'
import type { BookingMode } from './layoutTypes.ts'
import { useBookingMode } from './roomLayoutApi.ts'
import styles from './RoomLayout.module.css'

type Props = {
  roomId: string
  mode: BookingMode
  hasStations: boolean
  disabled: boolean
}

export function BookingModeForm({
  roomId,
  mode,
  hasStations,
  disabled,
}: Props) {
  const [choice, setChoice] = useState(mode)
  const update = useBookingMode(roomId)
  return (
    <form
      className={styles.panel}
      onSubmit={(event) => {
        event.preventDefault()
        update.submit(choice)
      }}
    >
      <h2 className={styles.panelTitle}>Modalidade de reserva</h2>
      <SelectField
        label="Como será reservado"
        value={choice}
        disabled={disabled || update.isPending}
        onChange={(event) => {
          if (
            event.target.value === 'WHOLE_SPACE' ||
            event.target.value === 'PER_STATION'
          )
            setChoice(event.target.value)
        }}
      >
        <option value="WHOLE_SPACE" disabled={hasStations}>
          Espaço inteiro
        </option>
        <option value="PER_STATION">Por estação</option>
      </SelectField>
      {hasStations && (
        <p className={styles.hint}>Esta sala possui estações vinculadas.</p>
      )}
      <ProblemNotice error={update.error} />
      <Button
        type="submit"
        disabled={disabled || update.isPending || choice === mode}
      >
        {update.isPending ? 'Salvando…' : 'Salvar modalidade'}
      </Button>
    </form>
  )
}
