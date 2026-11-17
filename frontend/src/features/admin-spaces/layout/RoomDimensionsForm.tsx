import { useState } from 'react'
import { Button } from '../../../shared/ui/Button.tsx'
import { TextField } from '../../../shared/ui/TextField.tsx'
import type { Dimensions } from './layoutGeometry.ts'
import styles from './RoomLayout.module.css'

type Props = {
  dimensions: Dimensions
  disabled: boolean
  onResize: (dimensions: Dimensions) => void
}

export function RoomDimensionsForm({ dimensions, disabled, onResize }: Props) {
  const [error, setError] = useState<string>()
  return (
    <form
      className={styles.panel}
      onSubmit={(event) => {
        event.preventDefault()
        const data = new FormData(event.currentTarget)
        const width = Number(String(data.get('width')).replace(',', '.'))
        const length = Number(String(data.get('length')).replace(',', '.'))
        if (
          ![width, length].every(
            (value) =>
              Number.isFinite(value) &&
              value >= 1 &&
              value <= 100 &&
              Math.abs(value * 100 - Math.round(value * 100)) < 0.00001,
          )
        ) {
          setError('Informe medidas de 1 a 100 m, com até duas casas decimais.')
          return
        }
        setError(undefined)
        onResize({ width, length })
      }}
    >
      <h2 className={styles.panelTitle}>Dimensões do mapa</h2>
      <TextField
        key={`width-${dimensions.width}`}
        label="Largura (m)"
        name="width"
        inputMode="decimal"
        defaultValue={String(dimensions.width).replace('.', ',')}
        disabled={disabled}
        error={error}
      />
      <TextField
        key={`length-${dimensions.length}`}
        label="Comprimento (m)"
        name="length"
        inputMode="decimal"
        defaultValue={String(dimensions.length).replace('.', ',')}
        disabled={disabled}
      />
      <Button type="submit" variant="secondary" disabled={disabled}>
        Aplicar medidas
      </Button>
      <p className={styles.hint}>
        Ajuste as medidas à sala real. As estações mantêm suas posições
        proporcionais.
      </p>
    </form>
  )
}
