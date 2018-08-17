package com.davidje13.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class RunnableThrowsMatcher extends TypeSafeDiagnosingMatcher<Runnable> {
	private final Matcher<? extends Exception> exceptionMatcher;

	private RunnableThrowsMatcher(Matcher<? extends Exception> exceptionMatcher) {
		this.exceptionMatcher = exceptionMatcher;
	}

	public static RunnableThrowsMatcher throwsException(Matcher<? extends Exception> exceptionMatcher) {
		return new RunnableThrowsMatcher(exceptionMatcher);
	}

	@Override
	protected boolean matchesSafely(Runnable runnable, Description mismatchDescription) {
		try {
			runnable.run();
		} catch(Exception e) {
			exceptionMatcher.describeMismatch(e, mismatchDescription);
			return exceptionMatcher.matches(e);
		}
		mismatchDescription.appendText("did not throw");
		return false;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("invocation to throw ");
		exceptionMatcher.describeTo(description);
	}
}
