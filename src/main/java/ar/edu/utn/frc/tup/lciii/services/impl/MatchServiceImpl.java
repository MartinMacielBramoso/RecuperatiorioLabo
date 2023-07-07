package ar.edu.utn.frc.tup.lciii.services.impl;

import ar.edu.utn.frc.tup.lciii.dtos.match.MatchResponseDTO;
import ar.edu.utn.frc.tup.lciii.dtos.match.NewMatchRequestDTO;
import ar.edu.utn.frc.tup.lciii.dtos.play.PlayRequestDTO;
import ar.edu.utn.frc.tup.lciii.dtos.play.PlayResponseDTO;
import ar.edu.utn.frc.tup.lciii.entities.MatchEntity;
import ar.edu.utn.frc.tup.lciii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.lciii.models.*;
import ar.edu.utn.frc.tup.lciii.repositories.jpa.MatchJpaRepository;
import ar.edu.utn.frc.tup.lciii.services.DeckService;
import ar.edu.utn.frc.tup.lciii.services.MatchService;
import ar.edu.utn.frc.tup.lciii.services.PlayerService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    private MatchJpaRepository matchJpaRepository;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private DeckService deckService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<MatchResponseDTO> getMatchesByPlayer(Long playerId) {
        List<MatchEntity> matchEntities = matchJpaRepository.getAllByPlayerOneOrPlayerTwo(playerId)
                .orElseThrow(() -> new EntityNotFoundException("No matches found for player with ID: " + playerId));

        return matchEntities.stream()
                .map(matchEntity -> modelMapper.map(matchEntity, MatchResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public MatchResponseDTO createMatch(NewMatchRequestDTO newMatchRequestDTO) {
        Player player1 = playerService.getPlayerById(newMatchRequestDTO.getPlayerOneId());
        Player player2 = playerService.getPlayerById(newMatchRequestDTO.getPlayerTwoId());

        Player player3 = playerService.getPlayerById(newMatchRequestDTO.getPlayerThreeId());

        // TODO: Terminar de implementar el metodo de manera tal que cree un Match nuevo entre dos jugadores.
        //  Si alguno de los jugadores no existe, la partida no puede iniciarse y debe retornarse una excepcion del tipo
        //  EntityNotFoundException con el mensaje "The user {userId} do not exist"
        //  Cuando se cre el Match, debe crearse el mazo (DeckService.createDeck) y mesclarlo (DeckService.shuffleDeck)
        //  El Match siempre arranca con el playerOne iniciando la partida, con el indice 1 nextCardIndex y lastCard
        //  con la primera carta del mazo y con status PLAYING
        if (player1 == null) {
            throw new EntityNotFoundException("The user " + newMatchRequestDTO.getPlayerOneId() + " does not exist");
        }
        if (player2 == null) {
            throw new EntityNotFoundException("The user " + newMatchRequestDTO.getPlayerTwoId() + " does not exist");
        }
        if (player3 == null) {
            throw new EntityNotFoundException("The user " + newMatchRequestDTO.getPlayerThreeId() + " does not exist");
        }
        
        player1.setStatus(PlayerStatus.EN_JUEGO);
        player2.setStatus(PlayerStatus.EN_JUEGO);
        player3.setStatus(PlayerStatus.EN_JUEGO);

        Deck deck = deckService.createDeck();
        deckService.shuffleDeck(deck);
        Card firstCard = deck.getCards().get(0);
        MatchEntity matchEntity = new MatchEntity();
        matchEntity.setDeck(deck);
        matchEntity.setLastCard(firstCard);
        matchEntity.setNextCardIndex(1);
        matchEntity.setPlayerOne(modelMapper.map(player1, PlayerEntity.class));
        matchEntity.setPlayerTwo(modelMapper.map(player2, PlayerEntity.class));

        matchEntity.setPlayerThree(modelMapper.map(player3, PlayerEntity.class));

        matchEntity.setNextToPlay(modelMapper.map(player1, PlayerEntity.class));
        matchEntity.setWinner(null);
        matchEntity.setMatchStatus(MatchStatus.PLAYING);
        System.out.println("proxima carta: "+deck.getCards().get(0)+"--"+ matchEntity.getNextCardIndex()+" "+deck.getCards().get(1));
        MatchEntity savedMatch = matchJpaRepository.save(matchEntity);
        return modelMapper.map(savedMatch, MatchResponseDTO.class);
    }

    @Override
    public Match getMatchById(Long id) {
        MatchEntity me = matchJpaRepository.getReferenceById(id);
        if(me != null) {
            Match match = modelMapper.map(me, Match.class);
            return match;
        }else {
            throw new EntityNotFoundException();
        }
    }

    @Override
    public MatchResponseDTO getMatchResponseDTOById(Long id) {
        MatchEntity me = matchJpaRepository.getReferenceById(id);
        if(me != null) {
            return modelMapper.map(me, MatchResponseDTO.class);
        }else {
            throw new EntityNotFoundException();
        }
    }

    @Transactional
    @Override
    public PlayResponseDTO play(Long matchId, PlayRequestDTO play) {
        PlayResponseDTO playResponseDTO = new PlayResponseDTO();
        Match match = this.getMatchById(matchId);
        // TODO: Terminar de implementar el metodo de manera tal que se ejecute la jugada siguiendo estas reglas:
        //  1 - Si el match no existe disparar una excepcion del tipo EntityNotFoundException
        //      con el mensaje "The match {matchId} do not exist"
        if(Objects.isNull(match.getDeck())) {
            throw new EntityNotFoundException("The match ID:"+ matchId+" do not exist");
        }
        //  2 - Si el jugador no existe disparar una excepcion del tipo EntityNotFoundException
        //      con el mensaje "The user {userId} do not exist"
        if(Objects.isNull(playerService.getPlayerById(play.getPlayer()).getUserName())){
            throw new EntityNotFoundException("The plyer ID:"+ match.getNextToPlay().getId()+" do not exist");
        }
        //  3 - Si el match ya terminó, disparar una excepcion del tipo MethodArgumentNotValidException
        //      con el mensaje "Game {gameId} is over"
        if(match.getMatchStatus().equals(MatchStatus.FINISH)){
            throw new EntityNotFoundException("Gme ID:"+match.getId()+"is over");
        }
        //  4 - Si el jugador que manda la jugada no es el proximo a jugar, disparar una excepcion del tipo MethodArgumentNotValidException
        //      con el mensaje "It is not the turn of the user {userName}"
        if(!match.getNextToPlay().getId().equals(play.getPlayer())){
            throw new EntityNotFoundException("It is not the turn of the user "
                    +playerService.getPlayerById(play.getPlayer()).getUserName());
        }
        //  5 - Si está OK, ejecutar la jugada haciendo lo siguiente:
        //      5.1 - Tomar el mazo de la partida y buscar la carta que sigue. Usar el metodo DeckService.takeCard
        Card card = deckService.takeCard(match.getDeck(),match.getNextCardIndex());
        System.out.println("carta: "+card.getCardSuit()+"--"+card.getValue());
        //      5.2 - Comparar si la carta tomada del mazo es mayor o menor que la ultima carta que se uso.
        //            Usar el metodo privado compareCards() de esta clase.
        int result = compareCards(card,match.getLastCard());
        System.out.println("compara: "+result+"++"+match.getLastCard());
        System.out.println("desision: "+play.getDecision().ordinal());
        //      5.3 - Comparar si el resultado de la comparacion de las cartas se condice con la decición del jugador
        if (result == play.getDecision().ordinal()) {
            //      5.4 - Si la respuesta es correcta (coinciden) el juego sigue y se debe actualizar
            //            la ultima carta recogida, el proximo jugador en jugar y el proximo indice de carta a recoger
            match.setNextCardIndex(match.getNextCardIndex()+1);
            if(match.getNextToPlay().equals(match.getPlayerOne())){
                if(match.getPlayerTwo().getStatus() == PlayerStatus.EN_JUEGO){
                    match.setNextToPlay(match.getPlayerTwo());
                }
                else-if(match.getPlayerThree().getStatus() == PlayerStatus.EN_JUEGO){
                    match.setNextToPlay(match.getPlayerThree());
                }
            }
            if(match.getNextToPlay().equals(match.getPlayerTwo())){
                if(match.getPlayerThree().getStatus() == PlayerStatus.EN_JUEGO){
                    match.setNextToPlay(match.getPlayerThree());
                }
                else-if(match.getPlayerOne().getStatus() == PlayerStatus.EN_JUEGO){
                    match.setNextToPlay(match.getPlayerOne());
                }
            }
            if(match.getNextToPlay().equals(match.getPlayerThree())){
                if(match.getPlayerOne().getStatus() == PlayerStatus.EN_JUEGO){
                    match.setNextToPlay(match.getPlayerOne());
                }
                else-if(match.getPlayerTwo().getStatus() == PlayerStatus.EN_JUEGO){
                    match.setNextToPlay(match.getPlayerTwo());
                }
            }
            
            //match.setNextToPlay((match.getNextToPlay().equals(match.getPlayerOne())
            //        ?match.getPlayerTwo():match.getPlayerOne()));                            *Codigo Viejo*
            match.setLastCard(match.getDeck().getCards().get(match.getNextCardIndex()));
        }
        //      5.5 - Si la respuesta no incorrecta (no coincide) el juego termina y se debe actualizar
        //            la ultima carta recogida, el proximo jugador en jugar, el proximo indice de carta a recoger, el ganador
        //            y el estado de la partida
        else{
            match.setLastCard(match.getDeck().getCards().get(match.getNextCardIndex()));
            match.setNextCardIndex(match.getNextCardIndex()+1);
            match.setWinner(match.getNextToPlay());
            match.getPlayer().setStatus(PlayerStatus.FUERA_JUEGO);
            if(match.getNextToPlay().equals(match.getPlayerOne())){
                if(match.getPlayerTwo().getStatus() == PlayerStatus.EN_JUEGO){
                    match.setNextToPlay(match.getPlayerTwo());
                }
                else-if(match.getPlayerThree().getStatus() == PlayerStatus.EN_JUEGO){
                    match.setNextToPlay(match.getPlayerThree());
                }
            }
            if(match.getNextToPlay().equals(match.getPlayerTwo())){
                if(match.getPlayerThree().getStatus() == PlayerStatus.EN_JUEGO){
                    match.setNextToPlay(match.getPlayerThree());
                }
                else-if(match.getPlayerOne().getStatus() == PlayerStatus.EN_JUEGO){
                    match.setNextToPlay(match.getPlayerOne());
                }
            }
            if(match.getNextToPlay().equals(match.getPlayerThree())){
                if(match.getPlayerOne().getStatus() == PlayerStatus.EN_JUEGO){
                    match.setNextToPlay(match.getPlayerOne());
                }
                else-if(match.getPlayerTwo().getStatus() == PlayerStatus.EN_JUEGO){
                    match.setNextToPlay(match.getPlayerTwo());
                }
            }
            //match.setNextToPlay((match.getNextToPlay().equals(match.getPlayerOne())
            //        ?match.getPlayerTwo():match.getPlayerOne()));
            match.setMatchStatus(MatchStatus.FINISH);
        }
        //      5.6 - Actualizar el Match
        matchJpaRepository.save(modelMapper.map(match, MatchEntity.class));
        //  6 - Como respuesta, se deben completar los datos de PlayResponseDTO y retornarlo.
        playResponseDTO.setCardsInDeck(match.getDeck().getCards().size()-match.getNextCardIndex());
        playResponseDTO.setDecision(play.getDecision());
        playResponseDTO.setMatchStatus(match.getMatchStatus());
        playResponseDTO.setPlayer(playerService.getPlayerById(play.getPlayer()).getId());
        playResponseDTO.setPreviousCard(match.getLastCard());
        playResponseDTO.setYourCard(match.getDeck().getCards().get(match.getNextCardIndex()));
        return playResponseDTO;
    }


    private Integer compareCards(Card card1, Card card2) {
        return card1.getNumber().compareTo(card2.getNumber());
    }
}
