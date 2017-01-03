package org.iipg.hurricane.util;

import java.io.IOException;

import org.iipg.hurricane.search.*;

/**
 * Creates queries from the {@link Analyzer} chain.
 * <p>
 * Example usage:
 * <pre class="prettyprint">
 *   QueryBuilder builder = new QueryBuilder(analyzer);
 *   Query a = builder.createBooleanQuery("body", "just a test");
 *   Query b = builder.createPhraseQuery("body", "another test");
 *   Query c = builder.createMinShouldMatchQuery("body", "another test", 0.5f);
 * </pre>
 * <p>
 * This can also be used as a subclass for query parsers to make it easier
 * to interact with the analysis chain. Factory methods such as {@code newTermQuery} 
 * are provided so that the generated queries can be customized.
 */
public class QueryBuilder {
	private boolean enablePositionIncrements = true;

	/** Creates a new QueryBuilder. */
	public QueryBuilder() {}

	/** 
	 * Creates a boolean query from the query text.
	 * <p>
	 * This is equivalent to {@code createBooleanQuery(field, queryText, Occur.SHOULD)}
	 * @param field field name
	 * @param queryText text to be passed to the analyzer
	 * @return {@code TermQuery} or {@code BooleanQuery}, based on the analysis
	 *         of {@code queryText}
	 */
	public Query createBooleanQuery(String field, String queryText) {
		return createBooleanQuery(field, queryText, BooleanClause.Occur.SHOULD);
	}

	/** 
	 * Creates a boolean query from the query text.
	 * <p>
	 * @param field field name
	 * @param queryText text to be passed to the analyzer
	 * @param operator operator used for clauses between analyzer tokens.
	 * @return {@code TermQuery} or {@code BooleanQuery}, based on the analysis 
	 *         of {@code queryText}
	 */
	public Query createBooleanQuery(String field, String queryText, BooleanClause.Occur operator) {
		if (operator != BooleanClause.Occur.SHOULD && operator != BooleanClause.Occur.MUST) {
			throw new IllegalArgumentException("invalid operator: only SHOULD or MUST are allowed");
		}
		return createFieldQuery(operator, field, queryText);
	}

	/** 
	 * Creates a phrase query from the query text.
	 * <p>
	 * This is equivalent to {@code createPhraseQuery(field, queryText, 0)}
	 * @param field field name
	 * @param queryText text to be passed to the analyzer
	 * @return {@code TermQuery}, {@code BooleanQuery}, {@code PhraseQuery}, or
	 *         {@code MultiPhraseQuery}, based on the analysis of {@code queryText}
	 */
	public Query createPhraseQuery(String field, String queryText) {
		return createFieldQuery(BooleanClause.Occur.MUST, field, queryText);
	}

	/** 
	 * Creates a minimum-should-match query from the query text.
	 * <p>
	 * @param field field name
	 * @param queryText text to be passed to the analyzer
	 * @param fraction of query terms {@code [0..1]} that should match 
	 * @return {@code TermQuery} or {@code BooleanQuery}, based on the analysis 
	 *         of {@code queryText}
	 */
	public Query createMinShouldMatchQuery(String field, String queryText, float fraction) {
		if (Float.isNaN(fraction) || fraction < 0 || fraction > 1) {
			throw new IllegalArgumentException("fraction should be >= 0 and <= 1");
		}

		// TODO: wierd that BQ equals/rewrite/scorer doesn't handle this?
		if (fraction == 1) {
			return createBooleanQuery(field, queryText, BooleanClause.Occur.MUST);
		}

		Query query = createFieldQuery(BooleanClause.Occur.SHOULD, field, queryText);
		if (query instanceof BooleanQuery) {
			BooleanQuery bq = (BooleanQuery) query;
			bq.setMinimumNumberShouldMatch((int) (fraction * bq.clauses().size()));
		}
		return query;
	}

	/**
	 * Returns true if position increments are enabled.
	 * @see #setEnablePositionIncrements(boolean)
	 */
	public boolean getEnablePositionIncrements() {
		return enablePositionIncrements;
	}

	/**
	 * Set to <code>true</code> to enable position increments in result query.
	 * <p>
	 * When set, result phrase and multi-phrase queries will
	 * be aware of position increments.
	 * Useful when e.g. a StopFilter increases the position increment of
	 * the token that follows an omitted token.
	 * <p>
	 * Default: true.
	 */
	public void setEnablePositionIncrements(boolean enable) {
		this.enablePositionIncrements = enable;
	}

	/**
	 * Creates a query from the analysis chain.
	 * <p>
	 * Expert: this is more useful for subclasses such as queryparsers. 
	 * If using this class directly, just use {@link #createBooleanQuery(String, String)}
	 * and {@link #createPhraseQuery(String, String)}
	 * @param operator default boolean operator used for this query
	 * @param field field to create queries against
	 * @param queryText text to be passed to the analysis chain
	 * @param quoted true if phrases should be generated when terms occur at more than one position
	 * @param phraseSlop slop factor for phrase/multiphrase queries
	 */
	protected final Query createFieldQuery(BooleanClause.Occur operator, String field, String queryText) {
		BooleanQuery bq = newBooleanQuery(true);
		bq.add(newTermQuery(new Term(field, queryText)), operator);
		return bq;
	}

	/**
	 * Builds a new BooleanQuery instance.
	 * <p>
	 * This is intended for subclasses that wish to customize the generated queries.
	 * @param disableCoord disable coord
	 * @return new BooleanQuery instance
	 */
	protected BooleanQuery newBooleanQuery(boolean disableCoord) {
		return new BooleanQuery(disableCoord);
	}

	/**
	 * Builds a new TermQuery instance.
	 * <p>
	 * This is intended for subclasses that wish to customize the generated queries.
	 * @param term term
	 * @return new TermQuery instance
	 */
	protected Query newTermQuery(Term term) {
		return new TermQuery(term);
	}

	/**
	 * Builds a new PhraseQuery instance.
	 * <p>
	 * This is intended for subclasses that wish to customize the generated queries.
	 * @return new PhraseQuery instance
	 */
	protected PhraseQuery newPhraseQuery() {
		return new PhraseQuery();
	}

	/**
	 * Builds a new MultiPhraseQuery instance.
	 * <p>
	 * This is intended for subclasses that wish to customize the generated queries.
	 * @return new MultiPhraseQuery instance
	 */
	protected MultiPhraseQuery newMultiPhraseQuery() {
		return new MultiPhraseQuery();
	}
}