
public class Linked_List<T> {
	private T value;
	private Linked_List<T> next;
	
	public Linked_List(T value)
	{
		this.value = value;
		this.next = null;
	}
	public Linked_List (T value , Linked_List<T> next)
	{
		this.value = value;
		this.next = next;
	}
	public T getValue() {
		return value;
	}
	public void setValue(T value) {
		this.value = value;
	}
	public Linked_List<T> getNext() {
		return next;
	}
	public void setNext(Linked_List<T> next) {
		this.next = next;
	}
	public void printList()
	{
		Linked_List<T> p = this;
		while(p!=null)
		{
			System.out.print(p.value + "==>");
			p=p.getNext();
		}
	}
	
}
