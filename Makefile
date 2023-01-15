CC=javac

make:
	$(CC) Bank.java

clean:
	rm Bank.class
	rm Account.class
	rm UI.class