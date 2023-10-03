# My first post
- Lorem ipsum dolor sit amet, consectetuer adipiscing elit.
- Lorem ipsum dolor sit amet, consectetuer adipiscing elit.
- Lorem ipsum dolor sit amet, consectetuer adipiscing elit.

and a para

1. and a numbered list
2. of sorts
3. and what not

``` clojure
(defn greetings
  "Prints a greeting to the given name or to the world."
  ([] (greatings "world"))
  ([name]
    (println "Hello, " name)))
```

``` lisp
(defun greetings (&optional (name "world"))
  (format t "Hello, ~A!~%" name))
```

``` python
def init(x):
  print(x)
```
