package at.jit.remind.core.model.status;

public enum State
{
	Unknown, Ok, Warning, Error;

	public State retrieveWorstState(State worstState)
	{
		if (this.ordinal() < worstState.ordinal())
		{
			return worstState;
		}
		else
		{
			return this;
		}
	}
}
