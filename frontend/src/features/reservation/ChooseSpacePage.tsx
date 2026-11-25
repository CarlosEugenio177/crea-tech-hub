import { useNavigate } from 'react-router'
import { Button } from '../../shared/ui/Button.tsx'
import { ButtonLink } from '../../shared/ui/ButtonLink.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { ProblemNotice } from '../../shared/ui/ProblemNotice.tsx'
import { QueryStatus } from '../../shared/ui/QueryStatus.tsx'
import styles from './ChooseSpacePage.module.css'
import { usePublicSpaces } from './publicSpacesApi.ts'
import { useReservationDraft } from './reservationDraft.ts'
import { RESERVE_RECOMMENDATION, RESERVE_TIME } from './reservationRoutes.ts'
import { SpaceOptionCard } from './SpaceOptionCard.tsx'
import { toSpaceOption } from './spaceOptions.ts'
import { StepPage } from './StepPage.tsx'

export function ChooseSpacePage() {
  const navigate = useNavigate()
  const { draft, update } = useReservationDraft()
  const spaces = usePublicSpaces()
  const options = spaces.data?.map(toSpaceOption)

  return (
    <StepPage
      step={1}
      title="Escolha um espaço"
      description="Escolha o ambiente e, em seguida, a data e o horário."
    >
      <div>
        <ButtonLink to={RESERVE_RECOMMENDATION} variant="secondary">
          Buscar por número de pessoas
        </ButtonLink>
      </div>
      {!options ? (
        <QueryStatus error={spaces.error} />
      ) : options.length === 0 ? (
        <Notice tone="info">Nenhum espaço está disponível no momento.</Notice>
      ) : (
        <ul className={styles.options}>
          {options.map((option) => (
            <SpaceOptionCard
              key={option.id}
              option={option}
              onChoose={() => {
                update({
                  space: { id: option.id, name: option.name },
                  station: undefined,
                  recommendation: undefined,
                  period:
                    draft.space?.id === option.id ? draft.period : undefined,
                })
                void navigate(RESERVE_TIME)
              }}
            />
          ))}
        </ul>
      )}
      {options && <ProblemNotice error={spaces.error} />}
      {spaces.hasNextPage && (
        <Button
          disabled={spaces.isFetchingNextPage}
          onClick={() => void spaces.fetchNextPage()}
        >
          Carregar mais espaços
        </Button>
      )}
    </StepPage>
  )
}
