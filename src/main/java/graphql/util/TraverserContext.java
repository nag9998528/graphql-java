package graphql.util;

import graphql.PublicApi;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Traversal context
 *
 * @param <T> type of tree node
 */
@PublicApi
public interface TraverserContext<T> {
    /**
     * Returns current node being visited
     *
     * @return current node traverser is visiting
     */
    T thisNode();

    /**
     * Returns parent context.
     * Effectively organizes Context objects in a linked list so
     * by following {@link #getParentContext() } links one could obtain
     * the current path as well as the variables {@link #getVar(java.lang.Class) }
     * stored in every parent context.
     *
     * @return context associated with the node parent
     */
    TraverserContext<T> getParentContext();

    /**
     * The result of the {@link #getParentContext()}.
     *
     * @return the parent result
     */
    default Object getParentResult() {
        return Optional
            .ofNullable(getParentContext())
            .map(TraverserContext::getResult)
            .orElse(null);
    }

    /**
     * Informs that the current node has been already "visited"
     *
     * @return {@code true} if a node had been already visited
     */
    default boolean isVisited() {
        return visitedNodes().contains(thisNode());
    }

    /**
     * Obtains all visited nodes and values received by the {@link TraverserVisitor#enter(graphql.util.TraverserContext) }
     * method
     *
     * @return a map containg all nodes visited and values passed when visiting nodes for the first time
     */
    Set<T> visitedNodes();

    /**
     * Obtains a context local variable
     *
     * @param <S> type of the variable
     * @param key key to lookup the variable value
     *
     * @return a variable value of {@code null}
     */
    default <S> S getVar(Class<? super S> key) {
        return computeVarIfAbsent(key, (context, k) -> null);
    }

    /**
     * Obtains a context variable or a default value if local variable is not present
     * 
     * @param <S>       type of the variable
     * @param key       key to lookup the variable value
     * @param provider  method to provide default value
     *
     * @return a variable value of {@code null}
     */
    <S> S computeVarIfAbsent (Class<? super S> key, BiFunction<? super TraverserContext<T>, ? super Class<S>, ? extends S> provider);
    
    /**
     * Stores a variable in the context
     *
     * @param <S>   type of a varable
     * @param key   key to create bindings for the variable
     * @param value value of variable
     *
     * @return this context to allow operations chaining
     */
    <S> TraverserContext<T> setVar(Class<? super S> key, S value);


    /**
     * Set the result for this TraverserContext.
     *
     * @param result to set
     */
    void setResult(Object result);

    /**
     * The result of this TraverserContext..
     *
     * @return the result
     */
    default Object getResult() {
        return computeResultIfAbsent(context -> null);
    }
    
    /**
     * The result of this TraverserContext or default value calculated using provided method
     * 
     * @param provider  method to provide default value
     * @return the result
     */
    Object computeResultIfAbsent (Function<? super TraverserContext<T>, ? extends Object> provider);

    /**
     * Used to share something across all TraverserContext.
     *
     * @return the initial data
     */
    Object getInitialData();
}
