/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphql.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a Vertex in a DependencyGraph
 * 
 * @param <N> the actual subtype of the Vertex
 */
public abstract class Vertex<N extends Vertex<N>> {
    public Object getId () {
        return id;
    }
    
    protected N id (Object id) {
        this.id = id;
        return (N)this;
    }
    
    public N dependsOn (N source, BiConsumer<? super N, ? super N> edgeAction) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(edgeAction);
        
        new Edge<>(source, (N)this, edgeAction)
            .connectEndpoints();
        
        return (N)this;
    }
    
    public N undependsOn (N source) {
        Objects.requireNonNull(source);
        
        new ArrayList<>(outdegrees)
            .stream()
            .filter(edge -> edge.getSource() == source)
            .forEach(Edge::disconnectEndpoints);
        
        return (N)this;
    }
    
    public N disconnect () {
        Stream.concat(
            new ArrayList<>(indegrees).stream(), 
            new ArrayList<>(outdegrees).stream()
        )
        .forEach(Edge::disconnectEndpoints);
        
        return (N)this;
    }
    
    public List<N> adjacencySet () {
        return indegrees
            .stream()
            .map(Edge::getSink)
            .collect(Collectors.toList());
    }
    
    public List<N> dependencySet () {
        return outdegrees
            .stream()
            .map(Edge::getSource)
            .collect(Collectors.toList());
    }
    
    public void fireResolved () {
        indegrees.forEach(Edge::fire);
    }
    
    public boolean canResolve () {
        return false;
    }
    
    public void resolve (N resultNode) {
        fireResolved();
    }
    
    @Override
    public String toString() {
        return toString(new StringBuilder(getClass().getSimpleName()).append('{'))
                .append('}')
                .toString();
    }
    
    protected StringBuilder toString (StringBuilder builder) {
        return builder
            .append("id=").append(id)
            .append(", dependencies=").append(
                outdegrees
                    .stream()
                    .map(Edge::getSource)
                    .map(Vertex::toString)
                    .collect(Collectors.joining(", ", "on ->", ""))
            );
    }
    
    protected Object id;
    protected final Set<Edge<N>> outdegrees = new LinkedHashSet<>();
    protected final Set<Edge<N>> indegrees = new LinkedHashSet<>();
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Vertex.class);
}
