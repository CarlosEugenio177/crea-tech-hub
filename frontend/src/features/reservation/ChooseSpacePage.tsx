import { useNavigate } from 'react-router'
import { Notice } from '../../shared/ui/Notice.tsx'
import styles from './ChooseSpacePage.module.css'
import { usePublicSpaces } from './publicSpacesApi.ts'
import { useReservationDraft } from './reservationDraft.ts'
import { RESERVE_TIME } from './reservationRoutes.ts'
import { SpaceOptionCard } from './SpaceOptionCard.tsx'
import { toSpaceOption } from './spaceOptions.ts'
import { StepPage } from './StepPage.tsx'
import { problemMessage } from '../../shared/api/problem.ts'

export function ChooseSpacePage() {
  const navigate = useNavigate()
  const { update } = useReservationDraft()
  const spaces = usePublicSpaces()
  const options = spaces.data?.items.map(toSpaceOption) ?? []

  return (
    <StepPage
      step={1}
      title="Qual espaço acompanha seu próximo projeto?"
      description="Escolha o ambiente e, em seguida, a data e o horário."
    >
      {spaces.isPending && <Notice tone="info">Carregando espaços...</Notice>}
      {spaces.error && (
        <Notice tone="error">{problemMessage(spaces.error)}</Notice>
      )}
      {!spaces.isPending && !spaces.error && options.length === 0 && (
        <Notice tone="info">Nenhum espaço está disponível no momento.</Notice>
      )}
      {options.length > 0 && (
        <ul className={styles.options}>
          {options.map((option) => (
            <SpaceOptionCard
              key={option.id}
              option={option}
              onChoose={() => {
                update({ spaceId: option.id, spaceName: option.name })
                void navigate(RESERVE_TIME)
              }}
            />
          ))}
        </ul>
      )}
    </StepPage>
  )
}
