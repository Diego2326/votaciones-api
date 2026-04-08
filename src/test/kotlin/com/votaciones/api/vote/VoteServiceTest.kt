package com.votaciones.api.vote

import com.votaciones.api.access.domain.TournamentAccessMode
import com.votaciones.api.access.dto.JoinByDisplayNameRequest
import com.votaciones.api.access.service.TournamentAccessService
import com.votaciones.api.common.exception.BadRequestException
import com.votaciones.api.common.exception.ConflictException
import com.votaciones.api.match.domain.MatchEntity
import com.votaciones.api.match.domain.MatchStatus
import com.votaciones.api.match.repository.MatchRepository
import com.votaciones.api.participant.domain.ParticipantEntity
import com.votaciones.api.participant.repository.ParticipantRepository
import com.votaciones.api.round.domain.RoundEntity
import com.votaciones.api.round.domain.RoundStatus
import com.votaciones.api.round.repository.RoundRepository
import com.votaciones.api.tournament.domain.TournamentEntity
import com.votaciones.api.tournament.domain.TournamentStatus
import com.votaciones.api.tournament.domain.TournamentType
import com.votaciones.api.tournament.repository.TournamentRepository
import com.votaciones.api.user.domain.RoleName
import com.votaciones.api.user.domain.UserEntity
import com.votaciones.api.user.repository.RoleRepository
import com.votaciones.api.user.repository.UserRepository
import com.votaciones.api.vote.dto.CastVoteRequest
import com.votaciones.api.vote.service.VoteService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VoteServiceTest(
    @Autowired private val voteService: VoteService,
    @Autowired private val tournamentAccessService: TournamentAccessService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val roleRepository: RoleRepository,
    @Autowired private val tournamentRepository: TournamentRepository,
    @Autowired private val participantRepository: ParticipantRepository,
    @Autowired private val roundRepository: RoundRepository,
    @Autowired private val matchRepository: MatchRepository,
) {

    @Test
    fun `should prevent duplicate vote on same match`() {
        val context = createVotingContext()

        voteService.castVote(context.match.id, context.sessionToken, CastVoteRequest(selectedParticipantId = context.participantA.id))
        val exception = assertThrows(ConflictException::class.java) {
            voteService.castVote(context.match.id, context.sessionToken, CastVoteRequest(selectedParticipantId = context.participantB.id))
        }

        assertEquals("Session has already voted on this match", exception.message)
    }

    @Test
    fun `should reject vote when round is not open`() {
        val context = createVotingContext(roundStatus = RoundStatus.CLOSED, matchStatus = MatchStatus.CLOSED)

        val exception = assertThrows(BadRequestException::class.java) {
            voteService.castVote(context.match.id, context.sessionToken, CastVoteRequest(selectedParticipantId = context.participantA.id))
        }

        assertEquals("Round must be OPEN to accept votes", exception.message)
    }

    private fun createVotingContext(
        roundStatus: RoundStatus = RoundStatus.OPEN,
        matchStatus: MatchStatus = MatchStatus.OPEN,
    ): VotingContext {
        val voterRole = roleRepository.findByName(RoleName.VOTER)!!
        val organizerRole = roleRepository.findByName(RoleName.ORGANIZER)!!

        val organizer = userRepository.save(
            UserEntity(
                username = "organizer_${System.nanoTime()}",
                email = "organizer_${System.nanoTime()}@example.com",
                passwordHash = "encoded",
                firstName = "Organizer",
                lastName = "User",
                roles = mutableSetOf(organizerRole),
            ),
        )

        val voter = userRepository.save(
            UserEntity(
                username = "voter_${System.nanoTime()}",
                email = "voter_${System.nanoTime()}@example.com",
                passwordHash = "encoded",
                firstName = "Vote",
                lastName = "User",
                roles = mutableSetOf(voterRole),
            ),
        )

        val tournament = tournamentRepository.save(
            TournamentEntity(
                title = "Test Tournament",
                type = TournamentType.ELIMINATION,
                status = TournamentStatus.ACTIVE,
                createdBy = organizer,
                accessMode = TournamentAccessMode.DISPLAY_NAME,
                joinPin = "123456",
                qrToken = "qr-test-token",
            ),
        )

        val participantA = participantRepository.save(ParticipantEntity(tournament = tournament, name = "Participant A"))
        val participantB = participantRepository.save(ParticipantEntity(tournament = tournament, name = "Participant B"))

        val round = roundRepository.save(
            RoundEntity(
                tournament = tournament,
                name = "Round 1",
                roundNumber = 1,
                status = roundStatus,
            ),
        )

        val match = matchRepository.save(
            MatchEntity(
                round = round,
                participantA = participantA,
                participantB = participantB,
                status = matchStatus,
            ),
        )

        val sessionToken = tournamentAccessService.joinByDisplayName(
            JoinByDisplayNameRequest(
                pin = tournament.joinPin,
                displayName = voter.firstName,
            ),
        ).sessionToken

        return VotingContext(
            participantA = participantA,
            participantB = participantB,
            match = match,
            sessionToken = sessionToken,
        )
    }

    private data class VotingContext(
        val participantA: ParticipantEntity,
        val participantB: ParticipantEntity,
        val match: MatchEntity,
        val sessionToken: String,
    )
}
