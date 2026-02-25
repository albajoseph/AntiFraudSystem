package antifraud.service;

import antifraud.model.StolenCard;
import antifraud.model.SuspiciousIP;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.SuspiciousIPRepository;
import antifraud.util.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BlacklistService {

    private final SuspiciousIPRepository ipRepository;
    private final StolenCardRepository cardRepository;

    public BlacklistService(SuspiciousIPRepository ipRepository, StolenCardRepository cardRepository) {
        this.ipRepository = ipRepository;
        this.cardRepository = cardRepository;
    }

    // --- Suspicious IP Logic ---

    public SuspiciousIP addIP(String ip) {
        if (!Validator.isValidIP(ip)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (ipRepository.existsByIp(ip)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        return ipRepository.save(new SuspiciousIP(ip));
    }

    public void deleteIP(String ip) {
        if (!Validator.isValidIP(ip)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        SuspiciousIP suspiciousIP = ipRepository.findByIp(ip)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ipRepository.delete(suspiciousIP);
    }

    public List<SuspiciousIP> listIPs() {
        return ipRepository.findAllByOrderByIdAsc();
    }

    // --- Stolen Card Logic ---

    public StolenCard addCard(String number) {
        if (!Validator.isValidLuhn(number)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (cardRepository.existsByNumber(number)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        return cardRepository.save(new StolenCard(number));
    }

    public void deleteCard(String number) {
        if (!Validator.isValidLuhn(number)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        StolenCard stolenCard = cardRepository.findByNumber(number)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        cardRepository.delete(stolenCard);
    }

    public List<StolenCard> listCards() {
        return cardRepository.findAllByOrderByIdAsc();
    }
}