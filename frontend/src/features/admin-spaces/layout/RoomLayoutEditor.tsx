import { useState } from 'react'
import { Button } from '../../../shared/ui/Button.tsx'
import { ProblemNotice } from '../../../shared/ui/ProblemNotice.tsx'
import { SelectField } from '../../../shared/ui/SelectField.tsx'
import type { Space, Station } from './layoutTypes.ts'
import { BookingModeForm } from './BookingModeForm.tsx'
import { StationBatchForm } from './StationBatchForm.tsx'
import { StationControls } from './StationControls.tsx'
import { StationMap } from './StationMap.tsx'
import { LayoutSelectionTools } from './LayoutSelectionTools.tsx'
import { RoomDimensionsForm } from './RoomDimensionsForm.tsx'
import { useSaveRoomLayout } from './roomLayoutApi.ts'
import { useRoomLayoutDraft } from './useRoomLayoutDraft.ts'
import styles from './RoomLayout.module.css'

type Props = {
  room: Space
  stations: Station[]
  onReload: () => void
  fetching: boolean
  loadError: unknown
}

export function RoomLayoutEditor({
  room,
  stations,
  onReload,
  fetching,
  loadError,
}: Props) {
  const { id: roomId, bookingMode: mode } = room
  const positions = useRoomLayoutDraft(room, stations)
  const [saved, setSaved] = useState(false)
  const [assisted, setAssisted] = useState(true)
  const [grid, setGrid] = useState(0.5)
  const save = useSaveRoomLayout(roomId, () => {
    positions.reset()
    setSaved(true)
  })
  const dirty = positions.dirty
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
          <RoomDimensionsForm
            dimensions={positions.dimensions}
            disabled={disabled}
            onResize={(dimensions) => {
              setSaved(false)
              positions.resize(dimensions)
            }}
          />
        )}
        {mode === 'PER_STATION' && (
          <StationBatchForm
            roomId={roomId}
            hasStations={stations.length > 0}
            disabled={disabled || dirty}
          />
        )}
        {dirty && (
          <p className={styles.hint}>
            Salve ou descarte o mapa antes de adicionar estações.
          </p>
        )}
      </aside>
      <section className={styles.mapPanel} aria-label="Edição do mapa">
        <div className={styles.mapHeader}>
          <h2 className={styles.panelTitle}>Mapa da sala</h2>
          <span className={styles.hint}>
            {positions.visible.length}{' '}
            {positions.visible.length === 1 ? 'estação' : 'estações'}
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
        ) : (
          <>
            <div className={styles.mapOptions}>
              <label className={styles.toggle}>
                <input
                  type="checkbox"
                  checked={assisted}
                  disabled={disabled}
                  onChange={(event) => setAssisted(event.target.checked)}
                />
                Auxiliar alinhamento
              </label>
              <SelectField
                label="Passo da grade"
                value={grid}
                disabled={disabled}
                onChange={(event) => setGrid(Number(event.target.value))}
              >
                <option value={0.25}>25 cm</option>
                <option value={0.5}>50 cm</option>
                <option value={1}>1 m</option>
              </SelectField>
            </div>
            <LayoutSelectionTools
              stations={positions.visible}
              selectedIds={positions.selectedIds}
              disabled={disabled}
              onSelect={positions.select}
              onSelectAll={positions.selectAll}
              onClear={positions.clearSelection}
              onArrange={(axis, distribute) => {
                setSaved(false)
                positions.arrange(axis, distribute)
              }}
              onRemove={() => {
                setSaved(false)
                positions.remove()
              }}
            />
            {positions.visible.length === 0 ? (
              <p className={styles.empty}>
                Informe a quantidade e gere as estações para começar.
              </p>
            ) : (
              <StationMap
                stations={positions.visible}
                selectedIds={positions.selectedIds}
                dimensions={positions.dimensions}
                assisted={assisted}
                grid={grid}
                disabled={disabled}
                point={positions.point}
                onSelect={positions.select}
                onRemove={(id) => {
                  setSaved(false)
                  positions.remove(id)
                }}
                onMove={(station, point) => {
                  setSaved(false)
                  positions.move(station, point)
                }}
              />
            )}
          </>
        )}
        {positions.removedCount > 0 && (
          <div className={styles.removalNotice}>
            <p className={styles.hint} role="status">
              {positions.removedCount} estações serão removidas do mapa ao
              salvar. Os cadastros serão desativados e preservados.
            </p>
            <Button
              variant="secondary"
              disabled={disabled}
              onClick={positions.undoRemoval}
            >
              Desfazer remoções
            </Button>
          </div>
        )}
        {positions.selected.length === 1 && positions.selected[0] && (
          <StationControls
            key={positions.selected[0].id}
            station={positions.selected[0]}
            point={positions.point(positions.selected[0])}
            disabled={disabled}
            dirty={dirty}
            onMove={(point) => {
              if (positions.selected[0]) {
                setSaved(false)
                positions.move(positions.selected[0], point)
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
              onClick={() => save.submit(positions.request)}
            >
              {save.isPending ? 'Salvando…' : 'Salvar mapa'}
            </Button>
          </div>
        </div>
      </section>
    </div>
  )
}
