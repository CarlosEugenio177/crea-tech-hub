import { useState } from 'react'
import { Button } from '../../../shared/ui/Button.tsx'
import { ProblemNotice } from '../../../shared/ui/ProblemNotice.tsx'
import type { BookingMode, Station } from './layoutTypes.ts'
import { BookingModeForm } from './BookingModeForm.tsx'
import { StationBatchForm } from './StationBatchForm.tsx'
import { StationControls } from './StationControls.tsx'
import { StationMap } from './StationMap.tsx'
import { useSavePositions } from './roomLayoutApi.ts'
import { useStationPositions } from './useStationPositions.ts'
import styles from './RoomLayout.module.css'

type Props = {
  roomId: string
  mode: BookingMode
  stations: Station[]
  onReload: () => void
  fetching: boolean
  loadError: unknown
}

export function RoomLayoutEditor({
  roomId,
  mode,
  stations,
  onReload,
  fetching,
  loadError,
}: Props) {
  const positions = useStationPositions(stations)
  const [saved, setSaved] = useState(false)
  const save = useSavePositions(roomId, () => {
    positions.reset()
    setSaved(true)
  })
  const dirty = positions.changes.length > 0
  const disabled = save.isPending || fetching || Boolean(loadError)
  return (
    <div className={styles.layout}>
      <aside className={styles.sidebar} aria-label="Configuração da sala">
        <BookingModeForm
          roomId={roomId}
          mode={mode}
          hasStations={stations.length > 0}
          disabled={disabled || dirty}
        />
        {mode === 'PER_STATION' && (
          <StationBatchForm
            roomId={roomId}
            hasStations={stations.length > 0}
            disabled={disabled || dirty}
          />
        )}
        {dirty && (
          <p className={styles.hint}>
            Salve ou descarte as posições antes de adicionar estações.
          </p>
        )}
      </aside>
      <section className={styles.mapPanel} aria-label="Edição do mapa">
        <div className={styles.mapHeader}>
          <h2 className={styles.panelTitle}>Mapa da sala</h2>
          <span className={styles.hint}>
            {stations.length} {stations.length === 1 ? 'estação' : 'estações'}
          </span>
        </div>
        <div className={styles.legend}>
          <span>● Ativa</span>
          <span className={styles.inactiveLegend}>● Inativa</span>
        </div>
        {mode === 'WHOLE_SPACE' ? (
          <p className={styles.empty}>
            Este espaço é reservado por inteiro. Selecione “Por estação” para
            montar um mapa.
          </p>
        ) : stations.length === 0 ? (
          <p className={styles.empty}>
            Informe a quantidade e gere as estações para começar.
          </p>
        ) : (
          <StationMap
            stations={stations}
            selectedId={positions.selected?.id}
            disabled={disabled}
            point={positions.point}
            onSelect={positions.select}
            onMove={(station, point) => {
              setSaved(false)
              positions.move(station, point)
            }}
          />
        )}
        {positions.selected && (
          <StationControls
            key={positions.selected.id}
            station={positions.selected}
            point={positions.point(positions.selected)}
            disabled={disabled}
            dirty={dirty}
            onMove={(point) => {
              if (positions.selected) {
                setSaved(false)
                positions.move(positions.selected, point)
              }
            }}
          />
        )}
        <ProblemNotice error={save.error} />
        <ProblemNotice error={loadError} />
        {save.error && (
          <Button
            variant="secondary"
            onClick={() => {
              positions.reset()
              onReload()
            }}
          >
            Descartar e recarregar
          </Button>
        )}
        {Boolean(loadError) && (
          <Button variant="secondary" onClick={onReload}>
            Recarregar mapa
          </Button>
        )}
        <div className={styles.saveBar}>
          <p role="status" className={styles.hint}>
            {dirty
              ? 'Alterações não salvas'
              : saved
                ? 'Mapa salvo.'
                : 'Nenhuma alteração pendente'}
          </p>
          <div className={styles.actions}>
            <Button
              variant="secondary"
              disabled={!dirty || disabled}
              onClick={() => positions.reset()}
            >
              Descartar
            </Button>
            <Button
              disabled={!dirty || disabled}
              onClick={() => save.submit(positions.changes)}
            >
              {save.isPending ? 'Salvando…' : 'Salvar mapa'}
            </Button>
          </div>
        </div>
      </section>
    </div>
  )
}
