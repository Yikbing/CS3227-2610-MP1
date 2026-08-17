package studytracker.parser;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import studytracker.command.AddCommand;
import studytracker.command.Command;
import studytracker.command.DeleteCommand;
import studytracker.command.EditCommand;
import studytracker.command.ExitCommand;
import studytracker.command.FilterCommand;
import studytracker.command.FindCommand;
import studytracker.command.HelpCommand;
import studytracker.command.ListCommand;
import studytracker.command.StatsCommand;
import studytracker.exception.ParseException;
import studytracker.model.EditDescriptor;
import studytracker.model.StudySession;

/** Converts raw user input into an executable command. */
public class Parser {
    public Command parse(String input) throws ParseException {
        if (input == null || input.isBlank()) {
            throw new ParseException("Please enter a command. Type 'help' for command formats.");
        }
        String stripped = input.strip();
        String[] parts = stripped.split("\\s+", 2);
        String word = parts[0].toLowerCase();
        String arguments = parts.length == 2 ? parts[1] : "";

        return switch (word) {
        case "add" -> parseAdd(arguments);
        case "list" -> noArguments(arguments, new ListCommand());
        case "edit" -> parseEdit(arguments);
        case "delete" -> new DeleteCommand(parseIndex(arguments));
        case "find" -> new FindCommand(ParserUtil.requireNonBlank(arguments, "Keyword"));
        case "filter" -> parseFilter(arguments);
        case "stats" -> noArguments(arguments, new StatsCommand());
        case "help" -> noArguments(arguments, new HelpCommand());
        case "exit" -> noArguments(arguments, new ExitCommand());
        default -> throw new ParseException("Unknown command '" + parts[0] + "'. Type 'help' for available commands.");
        };
    }

    private Command parseAdd(String arguments) throws ParseException {
        ArgumentMap fields = ArgumentMap.parse(arguments, Set.of("m", "t", "d", "on", "n"));
        String module = fields.required("m", "module");
        String topic = fields.required("t", "topic");
        int duration = ParserUtil.parsePositiveInt(fields.required("d", "duration"), "Duration");
        LocalDate date = ParserUtil.parseDate(fields.required("on", "date"), "Date");
        String notes = fields.optional("n").orElse("");
        try {
            return new AddCommand(new StudySession(module, topic, duration, date, notes));
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage());
        }
    }

    private Command parseEdit(String arguments) throws ParseException {
        String[] parts = arguments.strip().split("\\s+", 2);
        int index = parseIndex(parts.length == 0 ? "" : parts[0]);
        String fieldText = parts.length == 2 ? parts[1] : "";
        ArgumentMap fields = ArgumentMap.parse(fieldText, Set.of("m", "t", "d", "on", "n"));
        if (fields.isEmpty()) {
            throw new ParseException("Edit requires at least one field to change.");
        }
        Optional<String> module = validatedText(fields.optional("m"), "Module");
        Optional<String> topic = validatedText(fields.optional("t"), "Topic");
        Optional<Integer> duration = parseOptionalInt(fields.optional("d"), "Duration");
        Optional<LocalDate> date = parseOptionalDate(fields.optional("on"), "Date");
        Optional<String> notes = fields.optional("n");
        return new EditCommand(index, new EditDescriptor(module, topic, duration, date, notes));
    }

    private Command parseFilter(String arguments) throws ParseException {
        ArgumentMap fields = ArgumentMap.parse(arguments, Set.of("m", "from", "to"));
        if (fields.isEmpty()) {
            throw new ParseException("Filter requires m/MODULE, from/DATE, or to/DATE.");
        }
        String module = validatedText(fields.optional("m"), "Module").orElse(null);
        LocalDate from = parseOptionalDate(fields.optional("from"), "From date").orElse(null);
        LocalDate to = parseOptionalDate(fields.optional("to"), "To date").orElse(null);
        if (from != null && to != null && from.isAfter(to)) {
            throw new ParseException("The from date must not be after the to date.");
        }
        return new FilterCommand(module, from, to);
    }

    private int parseIndex(String value) throws ParseException {
        return ParserUtil.parsePositiveInt(value.strip(), "Session number");
    }

    private <T extends Command> T noArguments(String arguments, T command) throws ParseException {
        if (!arguments.isBlank()) {
            throw new ParseException("This command does not accept arguments.");
        }
        return command;
    }

    private Optional<String> validatedText(Optional<String> value, String label) throws ParseException {
        if (value.isPresent()) {
            return Optional.of(ParserUtil.requireNonBlank(value.get(), label));
        }
        return Optional.empty();
    }

    private Optional<Integer> parseOptionalInt(Optional<String> value, String label) throws ParseException {
        return value.isPresent()
                ? Optional.of(ParserUtil.parsePositiveInt(value.get(), label))
                : Optional.empty();
    }

    private Optional<LocalDate> parseOptionalDate(Optional<String> value, String label) throws ParseException {
        return value.isPresent()
                ? Optional.of(ParserUtil.parseDate(value.get(), label))
                : Optional.empty();
    }
}
