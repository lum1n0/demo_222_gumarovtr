document.addEventListener("DOMContentLoaded", () => {
    const slides = Array.from(document.querySelectorAll(".slide"));
    const nextButton = document.querySelector("[data-next]");
    const prevButton = document.querySelector("[data-prev]");

    if (slides.length === 0 || !nextButton || !prevButton) {
        return;
    }

    let active = 0;

    const showSlide = (nextIndex) => {
        slides[active].classList.remove("active");
        active = (nextIndex + slides.length) % slides.length;
        slides[active].classList.add("active");
    };

    nextButton.addEventListener("click", () => showSlide(active + 1));
    prevButton.addEventListener("click", () => showSlide(active - 1));
    setInterval(() => showSlide(active + 1), 3000);
});
