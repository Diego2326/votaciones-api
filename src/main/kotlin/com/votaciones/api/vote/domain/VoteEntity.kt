package com.votaciones.api.vote.domain

import com.votaciones.api.access.domain.TournamentJoinSessionEntity
import com.votaciones.api.common.entity.BaseEntity
import com.votaciones.api.match.domain.MatchEntity
import com.votaciones.api.participant.domain.ParticipantEntity
import com.votaciones.api.round.domain.RoundEntity
import com.votaciones.api.tournament.domain.TournamentEntity
import com.votaciones.api.user.domain.UserEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "votes",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_votes_match_join_session", columnNames = ["match_id", "join_session_id"]),
    ],
    indexes = [
        Index(name = "idx_votes_tournament", columnList = "tournament_id"),
        Index(name = "idx_votes_round", columnList = "round_id"),
        Index(name = "idx_votes_match", columnList = "match_id"),
        Index(name = "idx_votes_voter", columnList = "voter_id"),
        Index(name = "idx_votes_join_session", columnList = "join_session_id"),
    ],
)
class VoteEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    var tournament: TournamentEntity,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    var round: RoundEntity,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    var match: MatchEntity,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id")
    var voter: UserEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "join_session_id", nullable = false)
    var joinSession: TournamentJoinSessionEntity,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "selected_participant_id", nullable = false)
    var selectedParticipant: ParticipantEntity,
) : BaseEntity()
