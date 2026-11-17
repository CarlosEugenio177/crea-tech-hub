import { Button } from '../../../shared/ui/Button.tsx'
import type { Station } from './layoutTypes.ts'
import styles from './RoomLayout.module.css'

type Props = {
  stations: Station[]
  selectedIds: string[]
  disabled: boolean
  onSelect: (id: string, additive: boolean) => void
  onSelectAll: () => void
  onClear: () => void
  onArrange: (axis: 'x' | 'y', distribute: boolean) => void
  onRemove: () => void
}

export function LayoutSelectionTools({
  stations,
  selectedIds,
  disabled,
  onSelect,
  onSelectAll,
  onClear,
  onArrange,
  onRemove,
}: Props) {
  const count = selectedIds.filter((id) =>
    stations.some((station) => station.id === id),
  ).length
  return (
    <div className={styles.selectionTools}>
      <div className={styles.actions}>
        <Button
          variant="secondary"
          disabled={disabled || stations.length === 0}
          onClick={onSelectAll}
        >
          Selecionar todas
        </Button>
        <Button
          variant="secondary"
          disabled={disabled || count === 0}
          onClick={onClear}
        >
          Limpar seleção
        </Button>
        <span className={styles.hint}>{count} selecionadas</span>
      </div>
      <details>
        <summary>Selecionar por número</summary>
        <div className={styles.stationList}>
          {stations.map((station) => (
            <label key={station.id}>
              <input
                type="checkbox"
                disabled={disabled}
                checked={selectedIds.includes(station.id)}
                onChange={() => onSelect(station.id, true)}
              />
              Estação {station.stationPlacement.number}
            </label>
          ))}
        </div>
      </details>
      <div className={styles.actions}>
        <Button
          variant="secondary"
          disabled={disabled || count < 2}
          onClick={() => onArrange('y', false)}
        >
          Alinhar em fileira
        </Button>
        <Button
          variant="secondary"
          disabled={disabled || count < 2}
          onClick={() => onArrange('x', false)}
        >
          Alinhar em coluna
        </Button>
        <Button
          variant="secondary"
          disabled={disabled || count < 3}
          onClick={() => onArrange('x', true)}
        >
          Igualar distâncias horizontais
        </Button>
        <Button
          variant="secondary"
          disabled={disabled || count < 3}
          onClick={() => onArrange('y', true)}
        >
          Igualar distâncias verticais
        </Button>
        <button
          type="button"
          className={styles.removeButton}
          disabled={disabled || count === 0}
          onClick={onRemove}
        >
          Remover do mapa ({count})
        </button>
      </div>
    </div>
  )
}
