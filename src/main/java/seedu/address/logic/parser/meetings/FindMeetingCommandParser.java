package seedu.address.logic.parser.meetings;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PERSON_CONNECTION;

import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PRIORITY;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.meetings.FindMeetingCommand;
import seedu.address.logic.commands.persons.AddPersonCommand;
import seedu.address.logic.commands.persons.SortPersonCommand;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.Prefix;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.meeting.DateTime;
import seedu.address.model.meeting.Description;
import seedu.address.model.meeting.Meeting;
import seedu.address.model.meeting.MeetingName;
import seedu.address.model.meeting.Priority;


public class FindMeetingCommandParser implements Parser<FindMeetingCommand> {

    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).anyMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }


    /**
     * Parses {@code args} into a command and returns it.
     *
     * @param args
     * @throws ParseException if {@code userInput} does not conform the expected format
     */
    @Override
    public FindMeetingCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_PERSON_CONNECTION,
                        PREFIX_NAME, PREFIX_TIME, PREFIX_DESCRIPTION,
                        PREFIX_PRIORITY);

        if (!arePrefixesPresent(argMultimap, PREFIX_PERSON_CONNECTION,
                PREFIX_NAME, PREFIX_TIME, PREFIX_DESCRIPTION,
                PREFIX_PRIORITY)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(FindMeetingCommand.MESSAGE_USAGE);
        }

        List<String> personIndexes = argMultimap.getAllValues(PREFIX_PERSON_CONNECTION);
        List<String> meetingTimes = argMultimap.getAllValues(PREFIX_TIME);
        Optional<String> meetingName = argMultimap.getValue(PREFIX_NAME);
        Optional<String> meetingDescription = argMultimap.getValue(PREFIX_DESCRIPTION);
        Optional<String> meetingPriority = argMultimap.getValue(PREFIX_PRIORITY);

        try {

//            checkSomeNotNull(personIndexes,meetingTimes,meetingName,meetingDescription,
//                    meetingPriority);

            Set<Index> personsIndexesToSearch = getPersonsSet(personIndexes);

            Predicate<Meeting> predicateHasTimes = handleTimes(meetingTimes);
            Predicate<Meeting> predicateHasName = handleName(meetingName);
            Predicate<Meeting> predicateHasDescription = handleDescription(meetingDescription);
            Predicate<Meeting> predicateHasPriority = handlePriority(meetingPriority);

            Predicate<Meeting> bigPredicate = combinePredicates(predicateHasName,
                    predicateHasDescription,predicateHasPriority,predicateHasTimes);

            return new FindMeetingCommand(bigPredicate, personsIndexesToSearch);
        } catch (Exception e) {
            throw new ParseException(FindMeetingCommand.MESSAGE_USAGE);
        }

    }

    @SafeVarargs
    private Predicate<Meeting> combinePredicates(Predicate<Meeting> ... predicates) {
        Predicate<Meeting> combinedPredicate = Arrays.stream(predicates).reduce(pred -> true,
                (meetingPredicate, meetingPredicate2) -> meetingPredicate.and(meetingPredicate2));

        return combinedPredicate;
    }


    private Set<Index> getPersonsSet(List<String> personIndexes) throws ParseException {
        if (personIndexes.isEmpty()) {
            return new HashSet<>();
        }
        Set<Index> personIndexSet = ParserUtil.parsePersonsConnection(personIndexes);
        return personIndexSet;
    }


    private Predicate<Meeting> handleTimes(List<String> times) throws ParseException {
        if (times.isEmpty()) {
            return meeting -> true;
        }
        Set<DateTime> parsedTimes = ParserUtil.parseMeetingDateTimes(times);
        Predicate<Meeting> timePred = meeting -> parsedTimes.stream().allMatch(time ->
                meeting.hasTime(time));
        return timePred;
    }

    private Predicate<Meeting> handleName(Optional<String> name) throws ParseException {
        if (name.isEmpty()) {
            return meeting -> true;
        }
        MeetingName parsedNames = ParserUtil.parseMeetingName(name.get());
        Predicate<Meeting> namePred = meeting -> meeting.containsName(parsedNames);
        return namePred;
    }

    private Predicate<Meeting> handleDescription(Optional<String> description) throws ParseException {
        if (description.isEmpty()) {
            return meeting -> true;
        }
        Description parsedDesc = ParserUtil.parseMeetingDescription(description.get());
        Predicate<Meeting> pred = meeting -> meeting.containsDescription(parsedDesc);
        return pred;
    }

    private Predicate<Meeting> handlePriority(Optional<String> priority) throws ParseException {
        if (priority.isEmpty()) {
            return meeting -> true;
        }
        Priority parsedPrio = ParserUtil.parseMeetingPriority(priority.get());
        Predicate<Meeting> pred = meeting -> meeting.hasPriority(parsedPrio);
        return pred;
    }


    private void checkSomeNotNull(List list1, List list2, Optional ... optionals) throws ParseException {
        if (Arrays.stream(optionals).allMatch(o -> o.isEmpty()) && list1.isEmpty()
                && list2.isEmpty()) {
            throw new ParseException(FindMeetingCommand.MESSAGE_USAGE);
        }
    }





}
