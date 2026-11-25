import { useMemo, useState } from 'react'
import { Button } from '../../shared/ui/Button.tsx'
import { ButtonLink } from '../../shared/ui/ButtonLink.tsx'
import { ListPage } from '../../shared/ui/ListPage.tsx'
import { PagedList } from '../../shared/ui/PagedList.tsx'
import { SelectField } from '../../shared/ui/SelectField.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import { maskDate } from '../reservation/inputMasks.ts'
import {
  BRAZILIAN_DATE,
  isCalendarDate,
  toIsoDate,
} from '../reservation/reservationDates.ts'
import { SpaceBlockListItem } from './SpaceBlockListItem.tsx'
import { exclusiveDayEnd } from './spaceBlockDates.ts'
import styles from './SpaceBlocksPage.module.css'
import { ADMIN_SPACE_BLOCK_NEW } from './spaceBlockRoutes.ts'
import { useAllAdminSpaces, useSpaceBlocks } from './spaceBlocksApi.ts'

function parseFilterDate(value: string) {
  const trimmed = value.trim()
  if (trimmed === '') return { iso: undefined, error: undefined }
  if (!BRAZILIAN_DATE.test(trimmed)) {
    return { iso: undefined, error: 'Use dd/mm/aaaa.' }
  }
  const iso = toIsoDate(trimmed)
  if (!isCalendarDate(iso)) {
    return { iso: undefined, error: 'Data inválida.' }
  }
  return { iso, error: undefined }
}

export function SpaceBlocksPage() {
  const [spaceId, setSpaceId] = useState('')
  const [status, setStatus] = useState('true')
  const [fromInput, setFromInput] = useState('')
  const [toInput, setToInput] = useState('')
  const [page, setPage] = useState(0)

  const spaces = useAllAdminSpaces()
  const spaceNames = useMemo(
    () => new Map((spaces.data ?? []).map((space) => [space.id, space.name])),
    [spaces.data],
  )

  const fromParsed = parseFilterDate(fromInput)
  const toParsed = parseFilterDate(toInput)
  const rangeError =
    fromParsed.iso && toParsed.iso && toParsed.iso < fromParsed.iso
      ? 'A data final deve ser igual ou posterior à inicial.'
      : undefined

  const from =
    !rangeError && fromParsed.iso ? `${fromParsed.iso}T00:00` : undefined
  const to =
    !rangeError && toParsed.iso ? exclusiveDayEnd(toParsed.iso) : undefined
  const validDates = !rangeError && !fromParsed.error && !toParsed.error

  const blocks = useSpaceBlocks(
    {
      page,
      spaceId: spaceId || undefined,
      active: status === '' ? undefined : status === 'true',
      from,
      to,
    },
    validDates,
  )

  const hasFilters =
    spaceId !== '' || status !== 'true' || fromInput !== '' || toInput !== ''

  return (
    <ListPage
      eyebrow="Administração · Bloqueios"
      title="Bloqueios de horário"
      description="Bloqueios pontuais que impedem o uso de salas, mesas ou estações."
      actions={
        <ButtonLink to={ADMIN_SPACE_BLOCK_NEW}>Bloquear horário</ButtonLink>
      }
    >
      <div className={styles.filters}>
        <SelectField
          label="Espaço"
          value={spaceId}
          onChange={(event) => {
            setSpaceId(event.target.value)
            setPage(0)
          }}
        >
          <option value="">Todos os espaços</option>
          {(spaces.data ?? []).map((space) => (
            <option key={space.id} value={space.id}>
              {space.name}
            </option>
          ))}
        </SelectField>
        <SelectField
          label="Situação"
          value={status}
          onChange={(event) => {
            setStatus(event.target.value)
            setPage(0)
          }}
        >
          <option value="true">Ativos</option>
          <option value="false">Cancelados</option>
          <option value="">Todos</option>
        </SelectField>
        <TextField
          label="A partir de"
          inputMode="numeric"
          autoComplete="off"
          placeholder="dd/mm/aaaa"
          maxLength={10}
          value={fromInput}
          error={fromParsed.error}
          onChange={(event) => {
            setFromInput(maskDate(event.target.value))
            setPage(0)
          }}
        />
        <TextField
          label="Até"
          inputMode="numeric"
          autoComplete="off"
          placeholder="dd/mm/aaaa"
          maxLength={10}
          value={toInput}
          error={toParsed.error ?? rangeError}
          onChange={(event) => {
            setToInput(maskDate(event.target.value))
            setPage(0)
          }}
        />
      </div>
      {hasFilters && (
        <div className={styles.filterActions}>
          <Button
            variant="secondary"
            onClick={() => {
              setSpaceId('')
              setStatus('true')
              setFromInput('')
              setToInput('')
              setPage(0)
            }}
          >
            Limpar filtros
          </Button>
        </div>
      )}
      {validDates && (
        <PagedList
          data={blocks.data}
          error={blocks.error}
          page={page}
          onPageChange={setPage}
          emptyText={
            page === 0 && !hasFilters
              ? 'Nenhum bloqueio ativo.'
              : 'Nenhum bloqueio encontrado.'
          }
        >
          {(block) => (
            <SpaceBlockListItem
              key={block.id}
              block={block}
              spaceName={spaceNames.get(block.spaceId)}
            />
          )}
        </PagedList>
      )}
    </ListPage>
  )
}
