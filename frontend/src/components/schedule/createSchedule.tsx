import { PlusOutlined } from "@ant-design/icons";
import { Button, Form } from "antd";
import { useContext, useEffect, useState } from "react";
import { AuthContext } from "../../contexts/AuthContext";
import { useNotifications } from "../../hooks/useNotifications";
import { api } from "../../service/api";
import RightDrawer from "../table/rightDrawer";
import ScheduleForm, { dateTimeFormat } from "./scheduleForm";
import { ScheduleFormResponse } from "./scheduleList";
import dayjs from "dayjs";

export interface CreateEntityProps {
	fetchTable: () => void;
	date?: dayjs.Dayjs;
}

export default function CreateSchedule({ fetchTable, date }: CreateEntityProps) {
	const { user: loggedUser } = useContext(AuthContext);
	const [visible, setVisible] = useState(false);
	const [loading, setLoading] = useState(false);

	const [form] = Form.useForm();

	const notify = useNotifications();

	useEffect(() => {
		if (loggedUser) form.setFieldValue("responsibleId", loggedUser?.id);
	}, [loggedUser]);

	const saveAdd = async (schedule: ScheduleFormResponse) => {
		setLoading(true);

		await api
			.post("/schedule/add/", {
				roomId: schedule?.roomId,
				name: schedule?.name,
				responsibleId: schedule?.responsibleId,
				bookingStart: schedule.bookingStart.format(dateTimeFormat),
				bookingEnd: schedule.bookingEnd.format(dateTimeFormat),
			})
			.then((res) => {
				if (res.data.success) {
					notify.success("Agendamento criado com sucesso!");
					fetchTable();
				} else notify.error(res.data.errorMessage || "Erro");
			})
			.catch((err) => {
				notify.error(err.response.data.errorMessage || "Erro");
			})
			.finally(() => {
				setVisible(false);
				setLoading(false);
			});
	};

	return (
		<>
			<Button onClick={() => setVisible(true)} icon={<PlusOutlined />}>
				Criar agendamento
			</Button>

			<RightDrawer title="Criar agendamento" visible={visible} setVisible={setVisible} loading={loading} setLoading={setLoading} handleOk={() => form.submit()}>
				<ScheduleForm defaultDate={date} saveForm={saveAdd} form={form} loading={loading} setLoading={setLoading} />
			</RightDrawer>
		</>
	);
}
