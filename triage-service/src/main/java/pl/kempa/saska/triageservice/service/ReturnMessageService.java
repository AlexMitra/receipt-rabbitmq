package pl.kempa.saska.triageservice.service;

public interface ReturnMessageService<E> {
	void returnMessage(E entity, String exchange);
}
