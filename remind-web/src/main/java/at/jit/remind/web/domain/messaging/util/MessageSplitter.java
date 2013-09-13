package at.jit.remind.web.domain.messaging.util;

import java.util.ArrayList;
import java.util.List;

public class MessageSplitter
{
	public static List<String> splitMessageByLengthAndSpace(String message, int interval)
	{
		// define counters
		int firstIndex = 0;
		int defaultLastIndex = interval;
		int limit = message.length() - 1;
		int candidateForLastIndex = 0;

		List<String> listOfSplitedMessages = new ArrayList<String>();

		while (firstIndex <= limit)
		{
			if (defaultLastIndex >= limit)
			{
				listOfSplitedMessages.add(message.substring(firstIndex, message.length()));
				break;
			}

			String messageSubstring = message.substring(firstIndex, defaultLastIndex);

			if (messageSubstring.lastIndexOf(" ") != -1)
			{
				// we want to keep last space character on the end of the message
				candidateForLastIndex = messageSubstring.lastIndexOf(" ") + firstIndex + 1;
			}
			else
			{
				candidateForLastIndex = firstIndex + interval;
			}

			listOfSplitedMessages.add(message.substring(firstIndex, candidateForLastIndex));
			firstIndex = candidateForLastIndex;
			defaultLastIndex = firstIndex + interval;
		}

		return listOfSplitedMessages;
	}

}
