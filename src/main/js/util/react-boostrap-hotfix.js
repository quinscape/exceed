import { Modal } from "react-overlays";

const focus = () => {};

const cDU = Modal.prototype.componentDidUpdate;

Modal.prototype.componentDidUpdate = function (prevProps) {
    if (this.focus !== focus)
    {
        this.focus = focus;
    }
    cDU.call(this, prevProps);
};

export default Modal
