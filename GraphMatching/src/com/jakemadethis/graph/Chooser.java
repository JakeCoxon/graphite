package com.jakemadethis.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import edu.uci.ics.jung.graph.Hypergraph;

public class Chooser<T> {
	interface ChoiceListener<T> {
		void makeChoice(T item);
		void addChoice(T item);
		void removeChoice(T item);
	}
	
	private HashSet<T> items;
	private ArrayList<ChoiceListener<T>> choiceListeners = new ArrayList<Chooser.ChoiceListener<T>>();
	private T choice = null;
	
	public Chooser() {
		this(new ArrayList<T>());
	}
	public Chooser(Collection<T> collection) {
		items = new HashSet<T>(collection);
	}
	
	public void add(T item) {
		if (items.add(item)) {
			fireAddChoice(item);
			if (items.size() == 1) choose(item);
		}
	}
	
	public void remove(T item) {
		if (items.remove(item)) {
			fireRemoveChoice(item);
		}
	}
	
	public void choose(T item) {
		if (items.contains(item)) {
			choice = item;
			fireMakeChoice(item);
		}
	}
	
	public T getChoice() {
		return choice;
	}
	
	public HashSet<T> getItems() {
		return items;
	}
	
	public void addChoiceListener(ChoiceListener<T> listener) {
		choiceListeners.add(listener);
	}
	
	protected void fireMakeChoice(T item) {
		for (ChoiceListener<T> l : choiceListeners) {
			l.makeChoice(item);
		}
	}
	protected void fireAddChoice(T item) {
		for (ChoiceListener<T> l : choiceListeners) {
			l.addChoice(item);
		}
	}
	protected void fireRemoveChoice(T item) {
		for (ChoiceListener<T> l : choiceListeners) {
			l.removeChoice(item);
		}
	}
}
